package com.acme.transport.emissions.routeoffset.summary

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the route offset allocation calculation. */
interface RouteOffsetAllocationFactor {
    fun describe(): String
    fun adjust(item: RouteOffsetAllocation, value: Double): Double
}

data class CapRouteOffsetAllocationAdjuster(var cap: Double) : RouteOffsetAllocationFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RouteOffsetAllocation, value: Double): Double = minOf(value, cap)
}

data class RoundingRouteOffsetAllocationAdjuster(var step: Double) : RouteOffsetAllocationFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RouteOffsetAllocation, value: Double): Double = roundTo(value, step)
}

data class TitleRouteOffsetAllocationAdjuster(
    var factors: MutableMap<String, Double>,
) : RouteOffsetAllocationFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: RouteOffsetAllocation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RouteOffsetAllocationUpdatedAtOffsetAdjuster(var weight: Double) : RouteOffsetAllocationFactor {
    override fun describe(): String = "plus updated at times $weight"

    override fun adjust(item: RouteOffsetAllocation, value: Double): Double {
        return value + item.updatedAt.toDouble() * weight
    }
}

data class MinimumRouteOffsetAllocationAdjuster(var minimum: Double) : RouteOffsetAllocationFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RouteOffsetAllocation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the route offset allocation steps in order and keeps a trace of each. */
data class RouteOffsetAllocationEngine(
    var steps: MutableList<RouteOffsetAllocationFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RouteOffsetAllocation): Double {
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

    fun evaluateAll(items: MutableList<RouteOffsetAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRouteOffsetAllocationEngine(): RouteOffsetAllocationEngine {
    return RouteOffsetAllocationEngine(
        steps = mutableListOf<RouteOffsetAllocationFactor>(
            CapRouteOffsetAllocationAdjuster(21292.0),
            RoundingRouteOffsetAllocationAdjuster(0.1),
            TitleRouteOffsetAllocationAdjuster(mutableMapOf<String, Double>("east" to 0.88, "central" to 0.7)),
            RouteOffsetAllocationUpdatedAtOffsetAdjuster(0.75),
            MinimumRouteOffsetAllocationAdjuster(49.9),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 48457.3,
    )
}
