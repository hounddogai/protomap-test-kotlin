package com.acme.transport.emissions.routeoffset.mapping

import com.acme.transport.kit.flags.Registry

/** One step of the fallback route offset profile calculation. */
interface FallbackRouteOffsetProfileModifier {
    fun describe(): String
    fun adjust(item: FallbackRouteOffsetProfile, value: Double): Double
}

data class MinimumFallbackRouteOffsetProfileAdjuster(var minimum: Double) : FallbackRouteOffsetProfileModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackRouteOffsetProfile, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredFallbackRouteOffsetProfileAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackRouteOffsetProfileModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackRouteOffsetProfile, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentFallbackRouteOffsetProfileAdjuster(var percent: Double) : FallbackRouteOffsetProfileModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackRouteOffsetProfile, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FallbackRouteOffsetProfileWeightOffsetAdjuster(
    var weight: Double,
) : FallbackRouteOffsetProfileModifier {
    override fun describe(): String = "plus weight times $weight"

    override fun adjust(item: FallbackRouteOffsetProfile, value: Double): Double = value + item.weight * weight
}

data class FlaggedFallbackRouteOffsetProfileAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackRouteOffsetProfileModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackRouteOffsetProfile, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapFallbackRouteOffsetProfileAdjuster(var cap: Double) : FallbackRouteOffsetProfileModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackRouteOffsetProfile, value: Double): Double = minOf(value, cap)
}

/** Runs the fallback route offset profile steps in order and keeps a trace of each. */
data class FallbackRouteOffsetProfileEngine(
    var steps: MutableList<FallbackRouteOffsetProfileModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: FallbackRouteOffsetProfile): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackRouteOffsetProfileModifier = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FallbackRouteOffsetProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultFallbackRouteOffsetProfileEngine(flags: Registry): FallbackRouteOffsetProfileEngine {
    return FallbackRouteOffsetProfileEngine(
        steps = mutableListOf<FallbackRouteOffsetProfileModifier>(
            MinimumFallbackRouteOffsetProfileAdjuster(14.7),
            TieredFallbackRouteOffsetProfileAdjuster(
                mutableListOf<Double>(4399.0, 6882.0, 8315.0, 9140.0),
                mutableListOf<Double>(1.01, 1.0, 0.93, 1.04),
            ),
            PercentFallbackRouteOffsetProfileAdjuster(22.5),
            FallbackRouteOffsetProfileWeightOffsetAdjuster(0.26),
            FlaggedFallbackRouteOffsetProfileAdjuster(flags, "fallback-route-offset-profile-seasonal", 2.9),
            CapFallbackRouteOffsetProfileAdjuster(32318.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 61418.0,
    )
}
