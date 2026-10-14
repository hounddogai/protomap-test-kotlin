package com.acme.transport.terminal.appointment.preference

import com.acme.transport.kit.numbers.roundTo

/** One step of the appointment score calculation. */
interface AppointmentScoreAdjuster {
    fun describe(): String
    fun adjust(item: AppointmentScore, value: Double): Double
}

data class PercentAppointmentScoreAdjuster(var percent: Double) : AppointmentScoreAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AppointmentScore, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class AppointmentScoreDistanceKmOffsetAdjuster(var weight: Double) : AppointmentScoreAdjuster {
    override fun describe(): String = "plus distance km times $weight"

    override fun adjust(item: AppointmentScore, value: Double): Double = value + item.distanceKm * weight
}

data class RoundingAppointmentScoreAdjuster(var step: Double) : AppointmentScoreAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AppointmentScore, value: Double): Double = roundTo(value, step)
}

/** Runs the appointment score steps in order and keeps a trace of each. */
data class AppointmentScoreEngine(
    var steps: MutableList<AppointmentScoreAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AppointmentScore): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AppointmentScoreAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<AppointmentScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAppointmentScoreEngine(): AppointmentScoreEngine {
    return AppointmentScoreEngine(
        steps = mutableListOf<AppointmentScoreAdjuster>(
            PercentAppointmentScoreAdjuster(17.2),
            AppointmentScoreDistanceKmOffsetAdjuster(0.57),
            RoundingAppointmentScoreAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.1,
        ceiling = 56704.1,
    )
}
