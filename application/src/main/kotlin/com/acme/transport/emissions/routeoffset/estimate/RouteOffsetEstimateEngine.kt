package com.acme.transport.emissions.routeoffset.estimate

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the route offset estimate calculation. */
interface RouteOffsetEstimateStep {
    fun describe(): String
    fun adjust(item: RouteOffsetEstimate, value: Double): Double
}

data class LaneRouteOffsetEstimateAdjuster(var factors: MutableMap<String, Double>) : RouteOffsetEstimateStep {
    override fun describe(): String = "by lane"

    override fun adjust(item: RouteOffsetEstimate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingRouteOffsetEstimateAdjuster(var step: Double) : RouteOffsetEstimateStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RouteOffsetEstimate, value: Double): Double = roundTo(value, step)
}

data class PercentRouteOffsetEstimateAdjuster(var percent: Double) : RouteOffsetEstimateStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RouteOffsetEstimate, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the route offset estimate steps in order and keeps a trace of each. */
data class RouteOffsetEstimateEngine(
    var steps: MutableList<RouteOffsetEstimateStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RouteOffsetEstimate): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RouteOffsetEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRouteOffsetEstimateEngine(): RouteOffsetEstimateEngine {
    return RouteOffsetEstimateEngine(
        steps = mutableListOf<RouteOffsetEstimateStep>(
            LaneRouteOffsetEstimateAdjuster(mutableMapOf<String, Double>("retail" to 1.2)),
            RoundingRouteOffsetEstimateAdjuster(0.1),
            PercentRouteOffsetEstimateAdjuster(-18.7),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 44311.8,
    )
}
