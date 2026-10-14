package com.acme.transport.emissions.routeoffset.baseline

import com.acme.transport.kit.flags.Registry

/** One step of the route offset threshold calculation. */
interface RouteOffsetThresholdModifier {
    fun describe(): String
    fun adjust(item: RouteOffsetThreshold, value: Double): Double
}

data class FlaggedRouteOffsetThresholdAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RouteOffsetThresholdModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RouteOffsetThreshold, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumRouteOffsetThresholdAdjuster(var minimum: Double) : RouteOffsetThresholdModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RouteOffsetThreshold, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredRouteOffsetThresholdAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RouteOffsetThresholdModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RouteOffsetThreshold, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LabelRouteOffsetThresholdAdjuster(
    var factors: MutableMap<String, Double>,
) : RouteOffsetThresholdModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: RouteOffsetThreshold, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentRouteOffsetThresholdAdjuster(var percent: Double) : RouteOffsetThresholdModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RouteOffsetThreshold, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RouteOffsetThresholdCapacityOffsetAdjuster(var weight: Double) : RouteOffsetThresholdModifier {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: RouteOffsetThreshold, value: Double): Double = value + item.capacity.toDouble() * weight
}

/** Runs the route offset threshold steps in order and keeps a trace of each. */
data class RouteOffsetThresholdEngine(
    var steps: MutableList<RouteOffsetThresholdModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RouteOffsetThreshold): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RouteOffsetThresholdModifier = steps[index]
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

    fun evaluateAll(items: MutableList<RouteOffsetThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRouteOffsetThresholdEngine(flags: Registry): RouteOffsetThresholdEngine {
    return RouteOffsetThresholdEngine(
        steps = mutableListOf<RouteOffsetThresholdModifier>(
            FlaggedRouteOffsetThresholdAdjuster(flags, "route-offset-threshold-lenient", -8.3),
            MinimumRouteOffsetThresholdAdjuster(6.8),
            TieredRouteOffsetThresholdAdjuster(
                mutableListOf<Double>(2714.0, 6025.0, 6140.0, 8356.0),
                mutableListOf<Double>(1.18, 0.96, 1.11, 1.0),
            ),
            LabelRouteOffsetThresholdAdjuster(mutableMapOf<String, Double>("retail" to 0.81)),
            PercentRouteOffsetThresholdAdjuster(6.5),
            RouteOffsetThresholdCapacityOffsetAdjuster(1.95),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 32093.2,
    )
}
