package com.acme.transport.emissions.routeoffset.offsetcheckpoint

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the route offset preference calculation. */
interface RouteOffsetPreferenceAdjuster {
    fun describe(): String
    fun adjust(item: RouteOffsetPreference, value: Double): Double
}

data class RouteOffsetPreferenceDistanceKmOffsetAdjuster(var weight: Double) : RouteOffsetPreferenceAdjuster {
    override fun describe(): String = "plus distance km times $weight"

    override fun adjust(item: RouteOffsetPreference, value: Double): Double = value + item.distanceKm * weight
}

data class TieredRouteOffsetPreferenceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RouteOffsetPreferenceAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RouteOffsetPreference, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingRouteOffsetPreferenceAdjuster(var step: Double) : RouteOffsetPreferenceAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RouteOffsetPreference, value: Double): Double = roundTo(value, step)
}

data class FlaggedRouteOffsetPreferenceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RouteOffsetPreferenceAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RouteOffsetPreference, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumRouteOffsetPreferenceAdjuster(var minimum: Double) : RouteOffsetPreferenceAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RouteOffsetPreference, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the route offset preference steps in order and keeps a trace of each. */
data class RouteOffsetPreferenceEngine(
    var steps: MutableList<RouteOffsetPreferenceAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RouteOffsetPreference): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RouteOffsetPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRouteOffsetPreferenceEngine(flags: Registry): RouteOffsetPreferenceEngine {
    return RouteOffsetPreferenceEngine(
        steps = mutableListOf<RouteOffsetPreferenceAdjuster>(
            RouteOffsetPreferenceDistanceKmOffsetAdjuster(0.69),
            TieredRouteOffsetPreferenceAdjuster(
                mutableListOf<Double>(2520.0, 5170.0),
                mutableListOf<Double>(0.93, 1.07),
            ),
            RoundingRouteOffsetPreferenceAdjuster(5.0),
            FlaggedRouteOffsetPreferenceAdjuster(flags, "route-offset-preference-scheduled", 7.4),
            MinimumRouteOffsetPreferenceAdjuster(40.3),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 99875.8,
    )
}
