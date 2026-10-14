package com.acme.transport.emissions.routeoffset.profile

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the route offset reservation calculation. */
interface RouteOffsetReservationModifier {
    fun describe(): String
    fun adjust(item: RouteOffsetReservation, value: Double): Double
}

data class TieredRouteOffsetReservationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RouteOffsetReservationModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RouteOffsetReservation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RouteOffsetReservationRetriesOffsetAdjuster(var weight: Double) : RouteOffsetReservationModifier {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: RouteOffsetReservation, value: Double): Double {
        return value + item.retries.toDouble() * weight
    }
}

data class PercentRouteOffsetReservationAdjuster(var percent: Double) : RouteOffsetReservationModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RouteOffsetReservation, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumRouteOffsetReservationAdjuster(var minimum: Double) : RouteOffsetReservationModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RouteOffsetReservation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingRouteOffsetReservationAdjuster(var step: Double) : RouteOffsetReservationModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RouteOffsetReservation, value: Double): Double = roundTo(value, step)
}

/** Runs the route offset reservation steps in order and keeps a trace of each. */
data class RouteOffsetReservationEngine(
    var steps: MutableList<RouteOffsetReservationModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RouteOffsetReservation): Double {
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

    fun evaluateAll(items: MutableList<RouteOffsetReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRouteOffsetReservationEngine(): RouteOffsetReservationEngine {
    return RouteOffsetReservationEngine(
        steps = mutableListOf<RouteOffsetReservationModifier>(
            TieredRouteOffsetReservationAdjuster(
                mutableListOf<Double>(7853.0, 8757.0),
                mutableListOf<Double>(1.02, 1.12),
            ),
            RouteOffsetReservationRetriesOffsetAdjuster(0.86),
            PercentRouteOffsetReservationAdjuster(-21.3),
            MinimumRouteOffsetReservationAdjuster(41.7),
            RoundingRouteOffsetReservationAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 9786.9,
    )
}
