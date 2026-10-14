package com.acme.transport.emissions.routeoffset.rule

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the route offset adjustment calculation. */
interface RouteOffsetAdjustmentModifier {
    fun describe(): String
    fun adjust(item: RouteOffsetAdjustment, value: Double): Double
}

data class MinimumRouteOffsetAdjustmentAdjuster(var minimum: Double) : RouteOffsetAdjustmentModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RouteOffsetAdjustment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TitleRouteOffsetAdjustmentAdjuster(
    var factors: MutableMap<String, Double>,
) : RouteOffsetAdjustmentModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: RouteOffsetAdjustment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RouteOffsetAdjustmentStartsAtOffsetAdjuster(var weight: Double) : RouteOffsetAdjustmentModifier {
    override fun describe(): String = "plus starts at times $weight"

    override fun adjust(item: RouteOffsetAdjustment, value: Double): Double {
        return value + item.startsAt.toDouble() * weight
    }
}

data class CapRouteOffsetAdjustmentAdjuster(var cap: Double) : RouteOffsetAdjustmentModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RouteOffsetAdjustment, value: Double): Double = minOf(value, cap)
}

data class PercentRouteOffsetAdjustmentAdjuster(var percent: Double) : RouteOffsetAdjustmentModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RouteOffsetAdjustment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingRouteOffsetAdjustmentAdjuster(var step: Double) : RouteOffsetAdjustmentModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RouteOffsetAdjustment, value: Double): Double = roundTo(value, step)
}

/** Runs the route offset adjustment steps in order and keeps a trace of each. */
data class RouteOffsetAdjustmentEngine(
    var steps: MutableList<RouteOffsetAdjustmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RouteOffsetAdjustment): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RouteOffsetAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRouteOffsetAdjustmentEngine(): RouteOffsetAdjustmentEngine {
    return RouteOffsetAdjustmentEngine(
        steps = mutableListOf<RouteOffsetAdjustmentModifier>(
            MinimumRouteOffsetAdjustmentAdjuster(12.2),
            TitleRouteOffsetAdjustmentAdjuster(mutableMapOf<String, Double>("west" to 1.3, "south" to 0.95)),
            RouteOffsetAdjustmentStartsAtOffsetAdjuster(0.15),
            CapRouteOffsetAdjustmentAdjuster(41397.0),
            PercentRouteOffsetAdjustmentAdjuster(-11.7),
            RoundingRouteOffsetAdjustmentAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 43807.1,
    )
}
