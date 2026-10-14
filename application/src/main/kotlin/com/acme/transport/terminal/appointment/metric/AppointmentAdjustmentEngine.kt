package com.acme.transport.terminal.appointment.metric

/** One step of the appointment adjustment calculation. */
interface AppointmentAdjustmentModifier {
    fun describe(): String
    fun adjust(item: AppointmentAdjustment, value: Double): Double
}

data class TieredAppointmentAdjustmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AppointmentAdjustmentModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AppointmentAdjustment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class AppointmentAdjustmentExpiresAtOffsetAdjuster(var weight: Double) : AppointmentAdjustmentModifier {
    override fun describe(): String = "plus expires at times $weight"

    override fun adjust(item: AppointmentAdjustment, value: Double): Double {
        return value + item.expiresAt.toDouble() * weight
    }
}

data class CapAppointmentAdjustmentAdjuster(var cap: Double) : AppointmentAdjustmentModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AppointmentAdjustment, value: Double): Double = minOf(value, cap)
}

/** Runs the appointment adjustment steps in order and keeps a trace of each. */
data class AppointmentAdjustmentEngine(
    var steps: MutableList<AppointmentAdjustmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AppointmentAdjustment): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AppointmentAdjustmentModifier = steps[index]
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

    fun evaluateAll(items: MutableList<AppointmentAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAppointmentAdjustmentEngine(): AppointmentAdjustmentEngine {
    return AppointmentAdjustmentEngine(
        steps = mutableListOf<AppointmentAdjustmentModifier>(
            TieredAppointmentAdjustmentAdjuster(
                mutableListOf<Double>(1698.0, 3163.0, 6283.0, 9059.0),
                mutableListOf<Double>(0.86, 0.94, 1.2, 1.1),
            ),
            AppointmentAdjustmentExpiresAtOffsetAdjuster(1.56),
            CapAppointmentAdjustmentAdjuster(31232.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 49218.8,
    )
}
