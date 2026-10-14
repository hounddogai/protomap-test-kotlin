package com.acme.transport.emissions.routeoffset.notice

import com.acme.transport.kit.flags.Registry

/** One step of the route offset exception calculation. */
interface RouteOffsetExceptionFactor {
    fun describe(): String
    fun adjust(item: RouteOffsetException, value: Double): Double
}

data class MinimumRouteOffsetExceptionAdjuster(var minimum: Double) : RouteOffsetExceptionFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RouteOffsetException, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedRouteOffsetExceptionAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RouteOffsetExceptionFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RouteOffsetException, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapRouteOffsetExceptionAdjuster(var cap: Double) : RouteOffsetExceptionFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RouteOffsetException, value: Double): Double = minOf(value, cap)
}

data class UnitRouteOffsetExceptionAdjuster(
    var factors: MutableMap<String, Double>,
) : RouteOffsetExceptionFactor {
    override fun describe(): String = "by unit"

    override fun adjust(item: RouteOffsetException, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the route offset exception steps in order and keeps a trace of each. */
data class RouteOffsetExceptionEngine(
    var steps: MutableList<RouteOffsetExceptionFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RouteOffsetException): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RouteOffsetExceptionFactor = steps[index]
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

    fun evaluateAll(items: MutableList<RouteOffsetException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRouteOffsetExceptionEngine(flags: Registry): RouteOffsetExceptionEngine {
    return RouteOffsetExceptionEngine(
        steps = mutableListOf<RouteOffsetExceptionFactor>(
            MinimumRouteOffsetExceptionAdjuster(36.3),
            FlaggedRouteOffsetExceptionAdjuster(flags, "route-offset-exception-scheduled", -1.6),
            CapRouteOffsetExceptionAdjuster(15383.0),
            UnitRouteOffsetExceptionAdjuster(mutableMapOf<String, Double>("west" to 0.74)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 79511.4,
    )
}
