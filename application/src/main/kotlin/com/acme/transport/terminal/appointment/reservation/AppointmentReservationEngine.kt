package com.acme.transport.terminal.appointment.reservation

import com.acme.transport.kit.numbers.roundTo

/** One step of the appointment reservation calculation. */
interface AppointmentReservationModifier {
    fun describe(): String
    fun adjust(item: AppointmentReservation, value: Double): Double
}

data class MinimumAppointmentReservationAdjuster(var minimum: Double) : AppointmentReservationModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AppointmentReservation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredAppointmentReservationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AppointmentReservationModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AppointmentReservation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapAppointmentReservationAdjuster(var cap: Double) : AppointmentReservationModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AppointmentReservation, value: Double): Double = minOf(value, cap)
}

data class RoundingAppointmentReservationAdjuster(var step: Double) : AppointmentReservationModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AppointmentReservation, value: Double): Double = roundTo(value, step)
}

/** Runs the appointment reservation steps in order and keeps a trace of each. */
data class AppointmentReservationEngine(
    var steps: MutableList<AppointmentReservationModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AppointmentReservation): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AppointmentReservationModifier = steps[index]
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

    fun evaluateAll(items: MutableList<AppointmentReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAppointmentReservationEngine(): AppointmentReservationEngine {
    return AppointmentReservationEngine(
        steps = mutableListOf<AppointmentReservationModifier>(
            MinimumAppointmentReservationAdjuster(9.2),
            TieredAppointmentReservationAdjuster(
                mutableListOf<Double>(1946.0, 7502.0, 9441.0),
                mutableListOf<Double>(0.86, 0.89, 0.81),
            ),
            CapAppointmentReservationAdjuster(26333.0),
            RoundingAppointmentReservationAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 39100.3,
    )
}
