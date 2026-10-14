package com.acme.transport.emissions.routeoffset.window

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the route offset window calculation. */
interface RouteOffsetWindowAdjuster {
    fun describe(): String
    fun adjust(item: RouteOffsetWindow, value: Double): Double
}

data class MinimumRouteOffsetWindowAdjuster(var minimum: Double) : RouteOffsetWindowAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RouteOffsetWindow, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RouteOffsetWindowFloorValueOffsetAdjuster(var weight: Double) : RouteOffsetWindowAdjuster {
    override fun describe(): String = "plus floor value times $weight"

    override fun adjust(item: RouteOffsetWindow, value: Double): Double = value + item.floorValue * weight
}

data class CapRouteOffsetWindowAdjuster(var cap: Double) : RouteOffsetWindowAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RouteOffsetWindow, value: Double): Double = minOf(value, cap)
}

data class FlaggedRouteOffsetWindowAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RouteOffsetWindowAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RouteOffsetWindow, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingRouteOffsetWindowAdjuster(var step: Double) : RouteOffsetWindowAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RouteOffsetWindow, value: Double): Double = roundTo(value, step)
}

data class PercentRouteOffsetWindowAdjuster(var percent: Double) : RouteOffsetWindowAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RouteOffsetWindow, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the route offset window steps in order and keeps a trace of each. */
data class RouteOffsetWindowEngine(
    var steps: MutableList<RouteOffsetWindowAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RouteOffsetWindow): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RouteOffsetWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRouteOffsetWindowEngine(flags: Registry): RouteOffsetWindowEngine {
    return RouteOffsetWindowEngine(
        steps = mutableListOf<RouteOffsetWindowAdjuster>(
            MinimumRouteOffsetWindowAdjuster(33.1),
            RouteOffsetWindowFloorValueOffsetAdjuster(0.72),
            CapRouteOffsetWindowAdjuster(43003.0),
            FlaggedRouteOffsetWindowAdjuster(flags, "route-offset-window-fallback", 4.8),
            RoundingRouteOffsetWindowAdjuster(1.0),
            PercentRouteOffsetWindowAdjuster(6.4),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 16994.1,
    )
}
