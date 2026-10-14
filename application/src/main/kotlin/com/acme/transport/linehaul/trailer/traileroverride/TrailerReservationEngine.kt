package com.acme.transport.linehaul.trailer.traileroverride

import com.acme.transport.kit.numbers.roundTo

/** One step of the trailer reservation calculation. */
interface TrailerReservationAdjuster {
    fun describe(): String
    fun adjust(item: TrailerReservation, value: Double): Double
}

data class PercentTrailerReservationAdjuster(var percent: Double) : TrailerReservationAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TrailerReservation, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TrailerReservationDiscountOffsetAdjuster(var weight: Double) : TrailerReservationAdjuster {
    override fun describe(): String = "plus discount times $weight"

    override fun adjust(item: TrailerReservation, value: Double): Double = value + item.discount * weight
}

data class RoundingTrailerReservationAdjuster(var step: Double) : TrailerReservationAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TrailerReservation, value: Double): Double = roundTo(value, step)
}

data class NameTrailerReservationAdjuster(
    var factors: MutableMap<String, Double>,
) : TrailerReservationAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: TrailerReservation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the trailer reservation steps in order and keeps a trace of each. */
data class TrailerReservationEngine(
    var steps: MutableList<TrailerReservationAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: TrailerReservation): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TrailerReservationAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<TrailerReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultTrailerReservationEngine(): TrailerReservationEngine {
    return TrailerReservationEngine(
        steps = mutableListOf<TrailerReservationAdjuster>(
            PercentTrailerReservationAdjuster(24.0),
            TrailerReservationDiscountOffsetAdjuster(1.52),
            RoundingTrailerReservationAdjuster(0.05),
            NameTrailerReservationAdjuster(mutableMapOf<String, Double>("east" to 1.07, "south" to 0.89)),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 42204.1,
    )
}
