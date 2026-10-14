package com.acme.transport.emissions.routeoffset.offsetassignment

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the route offset budget calculation. */
interface RouteOffsetBudgetAdjuster {
    fun describe(): String
    fun adjust(item: RouteOffsetBudget, value: Double): Double
}

data class TieredRouteOffsetBudgetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RouteOffsetBudgetAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RouteOffsetBudget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumRouteOffsetBudgetAdjuster(var minimum: Double) : RouteOffsetBudgetAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RouteOffsetBudget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RouteOffsetBudgetCapacityOffsetAdjuster(var weight: Double) : RouteOffsetBudgetAdjuster {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: RouteOffsetBudget, value: Double): Double = value + item.capacity.toDouble() * weight
}

data class DepotRouteOffsetBudgetAdjuster(var factors: MutableMap<String, Double>) : RouteOffsetBudgetAdjuster {
    override fun describe(): String = "by depot"

    override fun adjust(item: RouteOffsetBudget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the route offset budget steps in order and keeps a trace of each. */
data class RouteOffsetBudgetEngine(
    var steps: MutableList<RouteOffsetBudgetAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RouteOffsetBudget): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RouteOffsetBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRouteOffsetBudgetEngine(): RouteOffsetBudgetEngine {
    return RouteOffsetBudgetEngine(
        steps = mutableListOf<RouteOffsetBudgetAdjuster>(
            TieredRouteOffsetBudgetAdjuster(
                mutableListOf<Double>(1937.0, 5990.0),
                mutableListOf<Double>(0.95, 0.88),
            ),
            MinimumRouteOffsetBudgetAdjuster(0.2),
            RouteOffsetBudgetCapacityOffsetAdjuster(0.48),
            DepotRouteOffsetBudgetAdjuster(mutableMapOf<String, Double>("north" to 0.95)),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 86509.7,
    )
}
