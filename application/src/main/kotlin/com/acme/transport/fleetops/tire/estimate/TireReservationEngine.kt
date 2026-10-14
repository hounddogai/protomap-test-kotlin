package com.acme.transport.fleetops.tire.estimate

import com.acme.transport.kit.numbers.roundTo

/** One step of the tire reservation calculation. */
interface TireReservationAdjuster {
    fun describe(): String
    fun adjust(item: TireReservation, value: Double): Double
}

data class RoundingTireReservationAdjuster(var step: Double) : TireReservationAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TireReservation, value: Double): Double = roundTo(value, step)
}

data class MinimumTireReservationAdjuster(var minimum: Double) : TireReservationAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TireReservation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredTireReservationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TireReservationAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TireReservation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class TireReservationRatioOffsetAdjuster(var weight: Double) : TireReservationAdjuster {
    override fun describe(): String = "plus ratio times $weight"

    override fun adjust(item: TireReservation, value: Double): Double = value + item.ratio * weight
}

data class CapTireReservationAdjuster(var cap: Double) : TireReservationAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TireReservation, value: Double): Double = minOf(value, cap)
}

/** Runs the tire reservation steps in order and keeps a trace of each. */
data class TireReservationEngine(
    var steps: MutableList<TireReservationAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TireReservation): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TireReservationAdjuster = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TireReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTireReservationEngine(): TireReservationEngine {
    return TireReservationEngine(
        steps = mutableListOf<TireReservationAdjuster>(
            RoundingTireReservationAdjuster(0.5),
            MinimumTireReservationAdjuster(46.4),
            TieredTireReservationAdjuster(
                mutableListOf<Double>(1238.0, 2390.0),
                mutableListOf<Double>(0.86, 1.09),
            ),
            TireReservationRatioOffsetAdjuster(0.25),
            CapTireReservationAdjuster(44179.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 24034.2,
    )
}
