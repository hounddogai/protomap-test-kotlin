package com.acme.transport.terminal.appointment.mapping

import com.acme.transport.kit.numbers.roundTo

/** One step of the appointment mapping calculation. */
interface AppointmentMappingAdjuster {
    fun describe(): String
    fun adjust(item: AppointmentMapping, value: Double): Double
}

data class MinimumAppointmentMappingAdjuster(var minimum: Double) : AppointmentMappingAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AppointmentMapping, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingAppointmentMappingAdjuster(var step: Double) : AppointmentMappingAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AppointmentMapping, value: Double): Double = roundTo(value, step)
}

data class CapAppointmentMappingAdjuster(var cap: Double) : AppointmentMappingAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AppointmentMapping, value: Double): Double = minOf(value, cap)
}

data class AppointmentMappingMarginOffsetAdjuster(var weight: Double) : AppointmentMappingAdjuster {
    override fun describe(): String = "plus margin times $weight"

    override fun adjust(item: AppointmentMapping, value: Double): Double = value + item.margin * weight
}

/** Runs the appointment mapping steps in order and keeps a trace of each. */
data class AppointmentMappingEngine(
    var steps: MutableList<AppointmentMappingAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AppointmentMapping): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AppointmentMappingAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<AppointmentMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAppointmentMappingEngine(): AppointmentMappingEngine {
    return AppointmentMappingEngine(
        steps = mutableListOf<AppointmentMappingAdjuster>(
            MinimumAppointmentMappingAdjuster(3.9),
            RoundingAppointmentMappingAdjuster(0.5),
            CapAppointmentMappingAdjuster(49656.0),
            AppointmentMappingMarginOffsetAdjuster(0.62),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 71731.4,
    )
}
