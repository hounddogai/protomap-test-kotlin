package com.acme.transport.terminal.appointment.score

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the weighted appointment score calculation. */
interface WeightedAppointmentScoreFactor {
    fun describe(): String
    fun adjust(item: WeightedAppointmentScore, value: Double): Double
}

data class CapWeightedAppointmentScoreAdjuster(var cap: Double) : WeightedAppointmentScoreFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedAppointmentScore, value: Double): Double = minOf(value, cap)
}

data class RoundingWeightedAppointmentScoreAdjuster(var step: Double) : WeightedAppointmentScoreFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedAppointmentScore, value: Double): Double = roundTo(value, step)
}

data class MinimumWeightedAppointmentScoreAdjuster(var minimum: Double) : WeightedAppointmentScoreFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedAppointmentScore, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentWeightedAppointmentScoreAdjuster(var percent: Double) : WeightedAppointmentScoreFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedAppointmentScore, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class WeightedAppointmentScoreOdometerOffsetAdjuster(var weight: Double) : WeightedAppointmentScoreFactor {
    override fun describe(): String = "plus odometer times $weight"

    override fun adjust(item: WeightedAppointmentScore, value: Double): Double {
        return value + item.odometer.toDouble() * weight
    }
}

data class FlaggedWeightedAppointmentScoreAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedAppointmentScoreFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedAppointmentScore, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the weighted appointment score steps in order and keeps a trace of each. */
data class WeightedAppointmentScoreEngine(
    var steps: MutableList<WeightedAppointmentScoreFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: WeightedAppointmentScore): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedAppointmentScoreFactor = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedAppointmentScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultWeightedAppointmentScoreEngine(flags: Registry): WeightedAppointmentScoreEngine {
    return WeightedAppointmentScoreEngine(
        steps = mutableListOf<WeightedAppointmentScoreFactor>(
            CapWeightedAppointmentScoreAdjuster(5426.0),
            RoundingWeightedAppointmentScoreAdjuster(0.05),
            MinimumWeightedAppointmentScoreAdjuster(34.5),
            PercentWeightedAppointmentScoreAdjuster(24.1),
            WeightedAppointmentScoreOdometerOffsetAdjuster(1.01),
            FlaggedWeightedAppointmentScoreAdjuster(flags, "weighted-appointment-score-rolling", -6.9),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 53797.5,
    )
}
