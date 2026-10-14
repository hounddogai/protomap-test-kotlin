package com.acme.transport.emissions.routeoffset.assignment

import com.acme.transport.kit.flags.Registry

/** One step of the route offset template calculation. */
interface RouteOffsetTemplateStep {
    fun describe(): String
    fun adjust(item: RouteOffsetTemplate, value: Double): Double
}

data class FlaggedRouteOffsetTemplateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RouteOffsetTemplateStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RouteOffsetTemplate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumRouteOffsetTemplateAdjuster(var minimum: Double) : RouteOffsetTemplateStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RouteOffsetTemplate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapRouteOffsetTemplateAdjuster(var cap: Double) : RouteOffsetTemplateStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RouteOffsetTemplate, value: Double): Double = minOf(value, cap)
}

data class RouteOffsetTemplateScoreOffsetAdjuster(var weight: Double) : RouteOffsetTemplateStep {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: RouteOffsetTemplate, value: Double): Double = value + item.score * weight
}

data class LocaleRouteOffsetTemplateAdjuster(
    var factors: MutableMap<String, Double>,
) : RouteOffsetTemplateStep {
    override fun describe(): String = "by locale"

    override fun adjust(item: RouteOffsetTemplate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentRouteOffsetTemplateAdjuster(var percent: Double) : RouteOffsetTemplateStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RouteOffsetTemplate, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the route offset template steps in order and keeps a trace of each. */
data class RouteOffsetTemplateEngine(
    var steps: MutableList<RouteOffsetTemplateStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RouteOffsetTemplate): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RouteOffsetTemplateStep = steps[index]
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

    fun evaluateAll(items: MutableList<RouteOffsetTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRouteOffsetTemplateEngine(flags: Registry): RouteOffsetTemplateEngine {
    return RouteOffsetTemplateEngine(
        steps = mutableListOf<RouteOffsetTemplateStep>(
            FlaggedRouteOffsetTemplateAdjuster(flags, "route-offset-template-pending", 7.0),
            MinimumRouteOffsetTemplateAdjuster(3.5),
            CapRouteOffsetTemplateAdjuster(1673.0),
            RouteOffsetTemplateScoreOffsetAdjuster(0.47),
            LocaleRouteOffsetTemplateAdjuster(
                mutableMapOf<String, Double>("central" to 1.16, "west" to 0.88, "online" to 1.15),
            ),
            PercentRouteOffsetTemplateAdjuster(24.2),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 11842.7,
    )
}
