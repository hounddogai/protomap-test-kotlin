package com.acme.transport.fleetops.tire.draft

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the tire draft calculation. */
interface TireDraftStep {
    fun describe(): String
    fun adjust(item: TireDraft, value: Double): Double
}

data class TieredTireDraftAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TireDraftStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TireDraft, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapTireDraftAdjuster(var cap: Double) : TireDraftStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TireDraft, value: Double): Double = minOf(value, cap)
}

data class TireDraftExpiresAtOffsetAdjuster(var weight: Double) : TireDraftStep {
    override fun describe(): String = "plus expires at times $weight"

    override fun adjust(item: TireDraft, value: Double): Double = value + item.expiresAt.toDouble() * weight
}

data class MinimumTireDraftAdjuster(var minimum: Double) : TireDraftStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TireDraft, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedTireDraftAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TireDraftStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TireDraft, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the tire draft steps in order and keeps a trace of each. */
data class TireDraftEngine(
    var steps: MutableList<TireDraftStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TireDraft): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TireDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTireDraftEngine(flags: Registry): TireDraftEngine {
    return TireDraftEngine(
        steps = mutableListOf<TireDraftStep>(
            TieredTireDraftAdjuster(
                mutableListOf<Double>(902.0, 5479.0, 7402.0, 9880.0),
                mutableListOf<Double>(0.91, 1.16, 1.11, 0.98),
            ),
            CapTireDraftAdjuster(29889.0),
            TireDraftExpiresAtOffsetAdjuster(1.65),
            MinimumTireDraftAdjuster(17.2),
            FlaggedTireDraftAdjuster(flags, "tire-draft-weighted", -2.4),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 49616.4,
    )
}
