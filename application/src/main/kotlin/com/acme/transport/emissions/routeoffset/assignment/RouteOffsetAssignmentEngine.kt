package com.acme.transport.emissions.routeoffset.assignment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the route offset assignment calculation. */
interface RouteOffsetAssignmentStep {
    fun describe(): String
    fun adjust(item: RouteOffsetAssignment, value: Double): Double
}

data class PercentRouteOffsetAssignmentAdjuster(var percent: Double) : RouteOffsetAssignmentStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RouteOffsetAssignment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LabelRouteOffsetAssignmentAdjuster(
    var factors: MutableMap<String, Double>,
) : RouteOffsetAssignmentStep {
    override fun describe(): String = "by label"

    override fun adjust(item: RouteOffsetAssignment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RouteOffsetAssignmentRetriesOffsetAdjuster(var weight: Double) : RouteOffsetAssignmentStep {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: RouteOffsetAssignment, value: Double): Double = value + item.retries.toDouble() * weight
}

data class RoundingRouteOffsetAssignmentAdjuster(var step: Double) : RouteOffsetAssignmentStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RouteOffsetAssignment, value: Double): Double = roundTo(value, step)
}

data class FlaggedRouteOffsetAssignmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RouteOffsetAssignmentStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RouteOffsetAssignment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumRouteOffsetAssignmentAdjuster(var minimum: Double) : RouteOffsetAssignmentStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RouteOffsetAssignment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the route offset assignment steps in order and keeps a trace of each. */
data class RouteOffsetAssignmentEngine(
    var steps: MutableList<RouteOffsetAssignmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RouteOffsetAssignment): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RouteOffsetAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRouteOffsetAssignmentEngine(flags: Registry): RouteOffsetAssignmentEngine {
    return RouteOffsetAssignmentEngine(
        steps = mutableListOf<RouteOffsetAssignmentStep>(
            PercentRouteOffsetAssignmentAdjuster(-13.0),
            LabelRouteOffsetAssignmentAdjuster(mutableMapOf<String, Double>("east" to 1.05, "central" to 0.7)),
            RouteOffsetAssignmentRetriesOffsetAdjuster(0.37),
            RoundingRouteOffsetAssignmentAdjuster(0.25),
            FlaggedRouteOffsetAssignmentAdjuster(flags, "route-offset-assignment-capped", -0.4),
            MinimumRouteOffsetAssignmentAdjuster(39.1),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 81916.0,
    )
}
