package com.acme.transport.emissions.routeoffset.segment

/** One step of the route offset milestone calculation. */
interface RouteOffsetMilestoneFactor {
    fun describe(): String
    fun adjust(item: RouteOffsetMilestone, value: Double): Double
}

data class RouteOffsetMilestoneScoreOffsetAdjuster(var weight: Double) : RouteOffsetMilestoneFactor {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: RouteOffsetMilestone, value: Double): Double = value + item.score * weight
}

data class TitleRouteOffsetMilestoneAdjuster(
    var factors: MutableMap<String, Double>,
) : RouteOffsetMilestoneFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: RouteOffsetMilestone, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapRouteOffsetMilestoneAdjuster(var cap: Double) : RouteOffsetMilestoneFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RouteOffsetMilestone, value: Double): Double = minOf(value, cap)
}

data class MinimumRouteOffsetMilestoneAdjuster(var minimum: Double) : RouteOffsetMilestoneFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RouteOffsetMilestone, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the route offset milestone steps in order and keeps a trace of each. */
data class RouteOffsetMilestoneEngine(
    var steps: MutableList<RouteOffsetMilestoneFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RouteOffsetMilestone): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RouteOffsetMilestoneFactor = steps[index]
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

    fun evaluateAll(items: MutableList<RouteOffsetMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRouteOffsetMilestoneEngine(): RouteOffsetMilestoneEngine {
    return RouteOffsetMilestoneEngine(
        steps = mutableListOf<RouteOffsetMilestoneFactor>(
            RouteOffsetMilestoneScoreOffsetAdjuster(1.67),
            TitleRouteOffsetMilestoneAdjuster(mutableMapOf<String, Double>("retail" to 1.02, "online" to 0.77)),
            CapRouteOffsetMilestoneAdjuster(1319.0),
            MinimumRouteOffsetMilestoneAdjuster(49.2),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 98688.7,
    )
}
