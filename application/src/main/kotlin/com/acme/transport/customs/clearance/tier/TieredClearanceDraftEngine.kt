package com.acme.transport.customs.clearance.tier

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the tiered clearance draft calculation. */
interface TieredClearanceDraftModifier {
    fun describe(): String
    fun adjust(item: TieredClearanceDraft, value: Double): Double
}

data class LaneTieredClearanceDraftAdjuster(
    var factors: MutableMap<String, Double>,
) : TieredClearanceDraftModifier {
    override fun describe(): String = "by lane"

    override fun adjust(item: TieredClearanceDraft, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedTieredClearanceDraftAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredClearanceDraftModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredClearanceDraft, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapTieredClearanceDraftAdjuster(var cap: Double) : TieredClearanceDraftModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredClearanceDraft, value: Double): Double = minOf(value, cap)
}

data class RoundingTieredClearanceDraftAdjuster(var step: Double) : TieredClearanceDraftModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredClearanceDraft, value: Double): Double = roundTo(value, step)
}

data class PercentTieredClearanceDraftAdjuster(var percent: Double) : TieredClearanceDraftModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredClearanceDraft, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredTieredClearanceDraftAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredClearanceDraftModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredClearanceDraft, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the tiered clearance draft steps in order and keeps a trace of each. */
data class TieredClearanceDraftEngine(
    var steps: MutableList<TieredClearanceDraftModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TieredClearanceDraft): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TieredClearanceDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTieredClearanceDraftEngine(flags: Registry): TieredClearanceDraftEngine {
    return TieredClearanceDraftEngine(
        steps = mutableListOf<TieredClearanceDraftModifier>(
            LaneTieredClearanceDraftAdjuster(mutableMapOf<String, Double>("east" to 1.02)),
            FlaggedTieredClearanceDraftAdjuster(flags, "tiered-clearance-draft-adaptive", -4.7),
            CapTieredClearanceDraftAdjuster(26687.0),
            RoundingTieredClearanceDraftAdjuster(0.1),
            PercentTieredClearanceDraftAdjuster(-4.0),
            TieredTieredClearanceDraftAdjuster(
                mutableListOf<Double>(1796.0, 1941.0, 5065.0, 6665.0),
                mutableListOf<Double>(0.82, 0.95, 0.8, 1.02),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 5029.9,
    )
}
