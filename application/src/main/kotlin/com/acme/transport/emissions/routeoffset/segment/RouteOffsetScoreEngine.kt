package com.acme.transport.emissions.routeoffset.segment

import com.acme.transport.kit.flags.Registry

/** One step of the route offset score calculation. */
interface RouteOffsetScoreStep {
    fun describe(): String
    fun adjust(item: RouteOffsetScore, value: Double): Double
}

data class DepotRouteOffsetScoreAdjuster(var factors: MutableMap<String, Double>) : RouteOffsetScoreStep {
    override fun describe(): String = "by depot"

    override fun adjust(item: RouteOffsetScore, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedRouteOffsetScoreAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RouteOffsetScoreStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RouteOffsetScore, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentRouteOffsetScoreAdjuster(var percent: Double) : RouteOffsetScoreStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RouteOffsetScore, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredRouteOffsetScoreAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RouteOffsetScoreStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RouteOffsetScore, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the route offset score steps in order and keeps a trace of each. */
data class RouteOffsetScoreEngine(
    var steps: MutableList<RouteOffsetScoreStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RouteOffsetScore): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RouteOffsetScoreStep = steps[index]
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

    fun evaluateAll(items: MutableList<RouteOffsetScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRouteOffsetScoreEngine(flags: Registry): RouteOffsetScoreEngine {
    return RouteOffsetScoreEngine(
        steps = mutableListOf<RouteOffsetScoreStep>(
            DepotRouteOffsetScoreAdjuster(mutableMapOf<String, Double>("north" to 0.74, "online" to 0.97)),
            FlaggedRouteOffsetScoreAdjuster(flags, "route-offset-score-seasonal", -2.2),
            PercentRouteOffsetScoreAdjuster(2.3),
            TieredRouteOffsetScoreAdjuster(
                mutableListOf<Double>(544.0, 3955.0, 6094.0),
                mutableListOf<Double>(1.18, 0.92, 0.88),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 11185.5,
    )
}
