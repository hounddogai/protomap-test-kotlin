package com.acme.transport.emissions.routeoffset.notice

import com.acme.transport.kit.flags.Registry

/** One step of the route offset checkpoint calculation. */
interface RouteOffsetCheckpointAdjuster {
    fun describe(): String
    fun adjust(item: RouteOffsetCheckpoint, value: Double): Double
}

data class CapRouteOffsetCheckpointAdjuster(var cap: Double) : RouteOffsetCheckpointAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RouteOffsetCheckpoint, value: Double): Double = minOf(value, cap)
}

data class RouteOffsetCheckpointScoreOffsetAdjuster(var weight: Double) : RouteOffsetCheckpointAdjuster {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: RouteOffsetCheckpoint, value: Double): Double = value + item.score * weight
}

data class PercentRouteOffsetCheckpointAdjuster(var percent: Double) : RouteOffsetCheckpointAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RouteOffsetCheckpoint, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedRouteOffsetCheckpointAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RouteOffsetCheckpointAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RouteOffsetCheckpoint, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the route offset checkpoint steps in order and keeps a trace of each. */
data class RouteOffsetCheckpointEngine(
    var steps: MutableList<RouteOffsetCheckpointAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RouteOffsetCheckpoint): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RouteOffsetCheckpointAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RouteOffsetCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRouteOffsetCheckpointEngine(flags: Registry): RouteOffsetCheckpointEngine {
    return RouteOffsetCheckpointEngine(
        steps = mutableListOf<RouteOffsetCheckpointAdjuster>(
            CapRouteOffsetCheckpointAdjuster(32544.0),
            RouteOffsetCheckpointScoreOffsetAdjuster(1.65),
            PercentRouteOffsetCheckpointAdjuster(21.3),
            FlaggedRouteOffsetCheckpointAdjuster(flags, "route-offset-checkpoint-manual", 9.4),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 58040.3,
    )
}
