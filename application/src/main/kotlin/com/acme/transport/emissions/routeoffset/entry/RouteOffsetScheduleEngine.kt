package com.acme.transport.emissions.routeoffset.entry

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the route offset schedule calculation. */
interface RouteOffsetScheduleModifier {
    fun describe(): String
    fun adjust(item: RouteOffsetSchedule, value: Double): Double
}

data class RoundingRouteOffsetScheduleAdjuster(var step: Double) : RouteOffsetScheduleModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RouteOffsetSchedule, value: Double): Double = roundTo(value, step)
}

data class MinimumRouteOffsetScheduleAdjuster(var minimum: Double) : RouteOffsetScheduleModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RouteOffsetSchedule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedRouteOffsetScheduleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RouteOffsetScheduleModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RouteOffsetSchedule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapRouteOffsetScheduleAdjuster(var cap: Double) : RouteOffsetScheduleModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RouteOffsetSchedule, value: Double): Double = minOf(value, cap)
}

data class TitleRouteOffsetScheduleAdjuster(
    var factors: MutableMap<String, Double>,
) : RouteOffsetScheduleModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: RouteOffsetSchedule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the route offset schedule steps in order and keeps a trace of each. */
data class RouteOffsetScheduleEngine(
    var steps: MutableList<RouteOffsetScheduleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RouteOffsetSchedule): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RouteOffsetScheduleModifier = steps[index]
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

    fun evaluateAll(items: MutableList<RouteOffsetSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRouteOffsetScheduleEngine(flags: Registry): RouteOffsetScheduleEngine {
    return RouteOffsetScheduleEngine(
        steps = mutableListOf<RouteOffsetScheduleModifier>(
            RoundingRouteOffsetScheduleAdjuster(1.0),
            MinimumRouteOffsetScheduleAdjuster(29.6),
            FlaggedRouteOffsetScheduleAdjuster(flags, "route-offset-schedule-pending", 2.2),
            CapRouteOffsetScheduleAdjuster(4516.0),
            TitleRouteOffsetScheduleAdjuster(
                mutableMapOf<String, Double>("east" to 0.97, "west" to 1.34, "central" to 1.03),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 32810.4,
    )
}
