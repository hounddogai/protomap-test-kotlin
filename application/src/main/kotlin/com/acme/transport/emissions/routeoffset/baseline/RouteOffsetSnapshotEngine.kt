package com.acme.transport.emissions.routeoffset.baseline

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the route offset snapshot calculation. */
interface RouteOffsetSnapshotAdjuster {
    fun describe(): String
    fun adjust(item: RouteOffsetSnapshot, value: Double): Double
}

data class NameRouteOffsetSnapshotAdjuster(
    var factors: MutableMap<String, Double>,
) : RouteOffsetSnapshotAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: RouteOffsetSnapshot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentRouteOffsetSnapshotAdjuster(var percent: Double) : RouteOffsetSnapshotAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RouteOffsetSnapshot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RouteOffsetSnapshotDurationMinutesOffsetAdjuster(var weight: Double) : RouteOffsetSnapshotAdjuster {
    override fun describe(): String = "plus duration minutes times $weight"

    override fun adjust(item: RouteOffsetSnapshot, value: Double): Double {
        return value + item.durationMinutes.toDouble() * weight
    }
}

data class CapRouteOffsetSnapshotAdjuster(var cap: Double) : RouteOffsetSnapshotAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RouteOffsetSnapshot, value: Double): Double = minOf(value, cap)
}

data class RoundingRouteOffsetSnapshotAdjuster(var step: Double) : RouteOffsetSnapshotAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RouteOffsetSnapshot, value: Double): Double = roundTo(value, step)
}

data class TieredRouteOffsetSnapshotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RouteOffsetSnapshotAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RouteOffsetSnapshot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the route offset snapshot steps in order and keeps a trace of each. */
data class RouteOffsetSnapshotEngine(
    var steps: MutableList<RouteOffsetSnapshotAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RouteOffsetSnapshot): Double {
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

    fun evaluateAll(items: MutableList<RouteOffsetSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRouteOffsetSnapshotEngine(): RouteOffsetSnapshotEngine {
    return RouteOffsetSnapshotEngine(
        steps = mutableListOf<RouteOffsetSnapshotAdjuster>(
            NameRouteOffsetSnapshotAdjuster(mutableMapOf<String, Double>("online" to 1.05)),
            PercentRouteOffsetSnapshotAdjuster(-8.9),
            RouteOffsetSnapshotDurationMinutesOffsetAdjuster(1.11),
            CapRouteOffsetSnapshotAdjuster(38687.0),
            RoundingRouteOffsetSnapshotAdjuster(1.0),
            TieredRouteOffsetSnapshotAdjuster(
                mutableListOf<Double>(1840.0, 7758.0),
                mutableListOf<Double>(1.11, 1.09),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 6608.7,
    )
}
