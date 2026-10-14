package com.acme.transport.terminal.appointment.bundle

import com.acme.transport.kit.flags.Registry

/** One step of the appointment override calculation. */
interface AppointmentOverrideModifier {
    fun describe(): String
    fun adjust(item: AppointmentOverride, value: Double): Double
}

data class MinimumAppointmentOverrideAdjuster(var minimum: Double) : AppointmentOverrideModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AppointmentOverride, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedAppointmentOverrideAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AppointmentOverrideModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AppointmentOverride, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class NameAppointmentOverrideAdjuster(
    var factors: MutableMap<String, Double>,
) : AppointmentOverrideModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: AppointmentOverride, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the appointment override steps in order and keeps a trace of each. */
data class AppointmentOverrideEngine(
    var steps: MutableList<AppointmentOverrideModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AppointmentOverride): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AppointmentOverrideModifier = steps[index]
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

    fun evaluateAll(items: MutableList<AppointmentOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAppointmentOverrideEngine(flags: Registry): AppointmentOverrideEngine {
    return AppointmentOverrideEngine(
        steps = mutableListOf<AppointmentOverrideModifier>(
            MinimumAppointmentOverrideAdjuster(29.5),
            FlaggedAppointmentOverrideAdjuster(flags, "appointment-override-lenient", 3.7),
            NameAppointmentOverrideAdjuster(mutableMapOf<String, Double>("west" to 0.81)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 64656.7,
    )
}
