package com.acme.transport.emissions.routeoffset.metric

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the route offset claim calculation. */
interface RouteOffsetClaimAdjuster {
    fun describe(): String
    fun adjust(item: RouteOffsetClaim, value: Double): Double
}

data class PercentRouteOffsetClaimAdjuster(var percent: Double) : RouteOffsetClaimAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RouteOffsetClaim, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RouteOffsetClaimCostOffsetAdjuster(var weight: Double) : RouteOffsetClaimAdjuster {
    override fun describe(): String = "plus cost times $weight"

    override fun adjust(item: RouteOffsetClaim, value: Double): Double = value + item.cost * weight
}

data class RoundingRouteOffsetClaimAdjuster(var step: Double) : RouteOffsetClaimAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RouteOffsetClaim, value: Double): Double = roundTo(value, step)
}

data class CapRouteOffsetClaimAdjuster(var cap: Double) : RouteOffsetClaimAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RouteOffsetClaim, value: Double): Double = minOf(value, cap)
}

/** Runs the route offset claim steps in order and keeps a trace of each. */
data class RouteOffsetClaimEngine(
    var steps: MutableList<RouteOffsetClaimAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RouteOffsetClaim): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RouteOffsetClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRouteOffsetClaimEngine(): RouteOffsetClaimEngine {
    return RouteOffsetClaimEngine(
        steps = mutableListOf<RouteOffsetClaimAdjuster>(
            PercentRouteOffsetClaimAdjuster(10.8),
            RouteOffsetClaimCostOffsetAdjuster(0.22),
            RoundingRouteOffsetClaimAdjuster(5.0),
            CapRouteOffsetClaimAdjuster(38146.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 88500.3,
    )
}
