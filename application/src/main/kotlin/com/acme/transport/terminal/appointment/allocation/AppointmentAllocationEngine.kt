package com.acme.transport.terminal.appointment.allocation

import com.acme.transport.kit.flags.Registry

/** One step of the appointment allocation calculation. */
interface AppointmentAllocationStep {
    fun describe(): String
    fun adjust(item: AppointmentAllocation, value: Double): Double
}

data class FlaggedAppointmentAllocationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AppointmentAllocationStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AppointmentAllocation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentAppointmentAllocationAdjuster(var percent: Double) : AppointmentAllocationStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AppointmentAllocation, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumAppointmentAllocationAdjuster(var minimum: Double) : AppointmentAllocationStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AppointmentAllocation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the appointment allocation steps in order and keeps a trace of each. */
data class AppointmentAllocationEngine(
    var steps: MutableList<AppointmentAllocationStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AppointmentAllocation): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AppointmentAllocationStep = steps[index]
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

    fun evaluateAll(items: MutableList<AppointmentAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAppointmentAllocationEngine(flags: Registry): AppointmentAllocationEngine {
    return AppointmentAllocationEngine(
        steps = mutableListOf<AppointmentAllocationStep>(
            FlaggedAppointmentAllocationAdjuster(flags, "appointment-allocation-default", 9.9),
            PercentAppointmentAllocationAdjuster(22.6),
            MinimumAppointmentAllocationAdjuster(46.8),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 24010.8,
    )
}
