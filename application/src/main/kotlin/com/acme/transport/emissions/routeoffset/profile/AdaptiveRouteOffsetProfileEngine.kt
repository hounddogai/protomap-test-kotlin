package com.acme.transport.emissions.routeoffset.profile

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the adaptive route offset profile calculation. */
interface AdaptiveRouteOffsetProfileAdjuster {
    fun describe(): String
    fun adjust(item: AdaptiveRouteOffsetProfile, value: Double): Double
}

data class PercentAdaptiveRouteOffsetProfileAdjuster(var percent: Double) : AdaptiveRouteOffsetProfileAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveRouteOffsetProfile, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedAdaptiveRouteOffsetProfileAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveRouteOffsetProfileAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveRouteOffsetProfile, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapAdaptiveRouteOffsetProfileAdjuster(var cap: Double) : AdaptiveRouteOffsetProfileAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveRouteOffsetProfile, value: Double): Double = minOf(value, cap)
}

data class TieredAdaptiveRouteOffsetProfileAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveRouteOffsetProfileAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveRouteOffsetProfile, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumAdaptiveRouteOffsetProfileAdjuster(var minimum: Double) : AdaptiveRouteOffsetProfileAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveRouteOffsetProfile, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the adaptive route offset profile steps in order and keeps a trace of each. */
data class AdaptiveRouteOffsetProfileEngine(
    var steps: MutableList<AdaptiveRouteOffsetProfileAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AdaptiveRouteOffsetProfile): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AdaptiveRouteOffsetProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAdaptiveRouteOffsetProfileEngine(flags: Registry): AdaptiveRouteOffsetProfileEngine {
    return AdaptiveRouteOffsetProfileEngine(
        steps = mutableListOf<AdaptiveRouteOffsetProfileAdjuster>(
            PercentAdaptiveRouteOffsetProfileAdjuster(-2.9),
            FlaggedAdaptiveRouteOffsetProfileAdjuster(flags, "adaptive-route-offset-profile-default", 3.8),
            CapAdaptiveRouteOffsetProfileAdjuster(49054.0),
            TieredAdaptiveRouteOffsetProfileAdjuster(
                mutableListOf<Double>(1055.0, 4516.0, 6198.0, 9364.0),
                mutableListOf<Double>(1.05, 1.11, 1.09, 0.87),
            ),
            MinimumAdaptiveRouteOffsetProfileAdjuster(25.4),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 12285.8,
    )
}
