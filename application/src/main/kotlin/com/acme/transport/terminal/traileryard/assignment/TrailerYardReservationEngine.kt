package com.acme.transport.terminal.traileryard.assignment

import com.acme.transport.kit.flags.Registry

/** One step of the trailer yard reservation calculation. */
interface TrailerYardReservationStep {
    fun describe(): String
    fun adjust(item: TrailerYardReservation, value: Double): Double
}

data class MinimumTrailerYardReservationAdjuster(var minimum: Double) : TrailerYardReservationStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TrailerYardReservation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapTrailerYardReservationAdjuster(var cap: Double) : TrailerYardReservationStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TrailerYardReservation, value: Double): Double = minOf(value, cap)
}

data class FlaggedTrailerYardReservationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TrailerYardReservationStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TrailerYardReservation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentTrailerYardReservationAdjuster(var percent: Double) : TrailerYardReservationStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TrailerYardReservation, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the trailer yard reservation steps in order and keeps a trace of each. */
data class TrailerYardReservationEngine(
    var steps: MutableList<TrailerYardReservationStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: TrailerYardReservation): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TrailerYardReservationStep = steps[index]
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

    fun evaluateAll(items: MutableList<TrailerYardReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultTrailerYardReservationEngine(flags: Registry): TrailerYardReservationEngine {
    return TrailerYardReservationEngine(
        steps = mutableListOf<TrailerYardReservationStep>(
            MinimumTrailerYardReservationAdjuster(15.0),
            CapTrailerYardReservationAdjuster(15193.0),
            FlaggedTrailerYardReservationAdjuster(flags, "trailer-yard-reservation-global", -1.8),
            PercentTrailerYardReservationAdjuster(3.2),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 24342.6,
    )
}
