package com.acme.transport.customs.clearance.rollup

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the tiered clearance preference calculation. */
interface TieredClearancePreferenceStep {
    fun describe(): String
    fun adjust(item: TieredClearancePreference, value: Double): Double
}

data class FlaggedTieredClearancePreferenceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredClearancePreferenceStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredClearancePreference, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapTieredClearancePreferenceAdjuster(var cap: Double) : TieredClearancePreferenceStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredClearancePreference, value: Double): Double = minOf(value, cap)
}

data class RoundingTieredClearancePreferenceAdjuster(var step: Double) : TieredClearancePreferenceStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredClearancePreference, value: Double): Double = roundTo(value, step)
}

data class RegionTieredClearancePreferenceAdjuster(
    var factors: MutableMap<String, Double>,
) : TieredClearancePreferenceStep {
    override fun describe(): String = "by region"

    override fun adjust(item: TieredClearancePreference, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the tiered clearance preference steps in order and keeps a trace of each. */
data class TieredClearancePreferenceEngine(
    var steps: MutableList<TieredClearancePreferenceStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: TieredClearancePreference): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TieredClearancePreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultTieredClearancePreferenceEngine(flags: Registry): TieredClearancePreferenceEngine {
    return TieredClearancePreferenceEngine(
        steps = mutableListOf<TieredClearancePreferenceStep>(
            FlaggedTieredClearancePreferenceAdjuster(flags, "tiered-clearance-preference-scheduled", 5.5),
            CapTieredClearancePreferenceAdjuster(29665.0),
            RoundingTieredClearancePreferenceAdjuster(0.5),
            RegionTieredClearancePreferenceAdjuster(
                mutableMapOf<String, Double>("north" to 0.72, "east" to 0.78, "south" to 1.38),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 6199.3,
    )
}
