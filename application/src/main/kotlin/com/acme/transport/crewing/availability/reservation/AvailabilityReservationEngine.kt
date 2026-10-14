package com.acme.transport.crewing.availability.reservation

import com.acme.transport.kit.numbers.roundTo

/** One step of the availability reservation calculation. */
interface AvailabilityReservationAdjuster {
    fun describe(): String
    fun adjust(item: AvailabilityReservation, value: Double): Double
}

data class LabelAvailabilityReservationAdjuster(
    var factors: MutableMap<String, Double>,
) : AvailabilityReservationAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: AvailabilityReservation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumAvailabilityReservationAdjuster(var minimum: Double) : AvailabilityReservationAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AvailabilityReservation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingAvailabilityReservationAdjuster(var step: Double) : AvailabilityReservationAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AvailabilityReservation, value: Double): Double = roundTo(value, step)
}

/** Runs the availability reservation steps in order and keeps a trace of each. */
data class AvailabilityReservationEngine(
    var steps: MutableList<AvailabilityReservationAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AvailabilityReservation): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AvailabilityReservationAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<AvailabilityReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAvailabilityReservationEngine(): AvailabilityReservationEngine {
    return AvailabilityReservationEngine(
        steps = mutableListOf<AvailabilityReservationAdjuster>(
            LabelAvailabilityReservationAdjuster(
                mutableMapOf<String, Double>("east" to 1.19, "retail" to 0.99, "north" to 1.3),
            ),
            MinimumAvailabilityReservationAdjuster(18.7),
            RoundingAvailabilityReservationAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 6560.6,
    )
}
