package com.acme.transport.emissions.routeoffset.rollup

import com.acme.transport.kit.numbers.roundTo

/** One step of the route offset profile calculation. */
interface RouteOffsetProfileStep {
    fun describe(): String
    fun adjust(item: RouteOffsetProfile, value: Double): Double
}

data class CapRouteOffsetProfileAdjuster(var cap: Double) : RouteOffsetProfileStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RouteOffsetProfile, value: Double): Double = minOf(value, cap)
}

data class PercentRouteOffsetProfileAdjuster(var percent: Double) : RouteOffsetProfileStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RouteOffsetProfile, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumRouteOffsetProfileAdjuster(var minimum: Double) : RouteOffsetProfileStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RouteOffsetProfile, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingRouteOffsetProfileAdjuster(var step: Double) : RouteOffsetProfileStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RouteOffsetProfile, value: Double): Double = roundTo(value, step)
}

data class TieredRouteOffsetProfileAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RouteOffsetProfileStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RouteOffsetProfile, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the route offset profile steps in order and keeps a trace of each. */
data class RouteOffsetProfileEngine(
    var steps: MutableList<RouteOffsetProfileStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RouteOffsetProfile): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RouteOffsetProfileStep = steps[index]
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

    fun evaluateAll(items: MutableList<RouteOffsetProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRouteOffsetProfileEngine(): RouteOffsetProfileEngine {
    return RouteOffsetProfileEngine(
        steps = mutableListOf<RouteOffsetProfileStep>(
            CapRouteOffsetProfileAdjuster(34336.0),
            PercentRouteOffsetProfileAdjuster(-11.8),
            MinimumRouteOffsetProfileAdjuster(31.3),
            RoundingRouteOffsetProfileAdjuster(0.05),
            TieredRouteOffsetProfileAdjuster(
                mutableListOf<Double>(1146.0, 5316.0),
                mutableListOf<Double>(1.13, 0.82),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 83187.3,
    )
}
