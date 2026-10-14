package com.acme.transport.emissions.routeoffset.rollup

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the route offset rollup calculation. */
interface RouteOffsetRollupAdjuster {
    fun describe(): String
    fun adjust(item: RouteOffsetRollup, value: Double): Double
}

data class MinimumRouteOffsetRollupAdjuster(var minimum: Double) : RouteOffsetRollupAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RouteOffsetRollup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RouteOffsetRollupFuelLitersOffsetAdjuster(var weight: Double) : RouteOffsetRollupAdjuster {
    override fun describe(): String = "plus fuel liters times $weight"

    override fun adjust(item: RouteOffsetRollup, value: Double): Double = value + item.fuelLiters * weight
}

data class FlaggedRouteOffsetRollupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RouteOffsetRollupAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RouteOffsetRollup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingRouteOffsetRollupAdjuster(var step: Double) : RouteOffsetRollupAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RouteOffsetRollup, value: Double): Double = roundTo(value, step)
}

/** Runs the route offset rollup steps in order and keeps a trace of each. */
data class RouteOffsetRollupEngine(
    var steps: MutableList<RouteOffsetRollupAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RouteOffsetRollup): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RouteOffsetRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRouteOffsetRollupEngine(flags: Registry): RouteOffsetRollupEngine {
    return RouteOffsetRollupEngine(
        steps = mutableListOf<RouteOffsetRollupAdjuster>(
            MinimumRouteOffsetRollupAdjuster(12.4),
            RouteOffsetRollupFuelLitersOffsetAdjuster(1.93),
            FlaggedRouteOffsetRollupAdjuster(flags, "route-offset-rollup-pending", 5.1),
            RoundingRouteOffsetRollupAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 72123.7,
    )
}
