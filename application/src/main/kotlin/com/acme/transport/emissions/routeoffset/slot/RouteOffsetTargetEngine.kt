package com.acme.transport.emissions.routeoffset.slot

import com.acme.transport.kit.flags.Registry

/** One step of the route offset target calculation. */
interface RouteOffsetTargetModifier {
    fun describe(): String
    fun adjust(item: RouteOffsetTarget, value: Double): Double
}

data class SourceRouteOffsetTargetAdjuster(
    var factors: MutableMap<String, Double>,
) : RouteOffsetTargetModifier {
    override fun describe(): String = "by source"

    override fun adjust(item: RouteOffsetTarget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapRouteOffsetTargetAdjuster(var cap: Double) : RouteOffsetTargetModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RouteOffsetTarget, value: Double): Double = minOf(value, cap)
}

data class MinimumRouteOffsetTargetAdjuster(var minimum: Double) : RouteOffsetTargetModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RouteOffsetTarget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedRouteOffsetTargetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RouteOffsetTargetModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RouteOffsetTarget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the route offset target steps in order and keeps a trace of each. */
data class RouteOffsetTargetEngine(
    var steps: MutableList<RouteOffsetTargetModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RouteOffsetTarget): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RouteOffsetTargetModifier = steps[index]
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

    fun evaluateAll(items: MutableList<RouteOffsetTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRouteOffsetTargetEngine(flags: Registry): RouteOffsetTargetEngine {
    return RouteOffsetTargetEngine(
        steps = mutableListOf<RouteOffsetTargetModifier>(
            SourceRouteOffsetTargetAdjuster(
                mutableMapOf<String, Double>("retail" to 0.84, "west" to 1.01, "east" to 0.94),
            ),
            CapRouteOffsetTargetAdjuster(12772.0),
            MinimumRouteOffsetTargetAdjuster(6.8),
            FlaggedRouteOffsetTargetAdjuster(flags, "route-offset-target-adaptive", 3.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 82552.4,
    )
}
