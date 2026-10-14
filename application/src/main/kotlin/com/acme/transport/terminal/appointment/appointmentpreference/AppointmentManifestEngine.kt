package com.acme.transport.terminal.appointment.appointmentpreference

import com.acme.transport.kit.numbers.roundTo

/** One step of the appointment manifest calculation. */
interface AppointmentManifestAdjuster {
    fun describe(): String
    fun adjust(item: AppointmentManifest, value: Double): Double
}

data class CountryAppointmentManifestAdjuster(
    var factors: MutableMap<String, Double>,
) : AppointmentManifestAdjuster {
    override fun describe(): String = "by country"

    override fun adjust(item: AppointmentManifest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapAppointmentManifestAdjuster(var cap: Double) : AppointmentManifestAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AppointmentManifest, value: Double): Double = minOf(value, cap)
}

data class RoundingAppointmentManifestAdjuster(var step: Double) : AppointmentManifestAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AppointmentManifest, value: Double): Double = roundTo(value, step)
}

/** Runs the appointment manifest steps in order and keeps a trace of each. */
data class AppointmentManifestEngine(
    var steps: MutableList<AppointmentManifestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AppointmentManifest): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AppointmentManifestAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<AppointmentManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAppointmentManifestEngine(): AppointmentManifestEngine {
    return AppointmentManifestEngine(
        steps = mutableListOf<AppointmentManifestAdjuster>(
            CountryAppointmentManifestAdjuster(mutableMapOf<String, Double>("south" to 1.37, "east" to 1.08)),
            CapAppointmentManifestAdjuster(33099.0),
            RoundingAppointmentManifestAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 15462.7,
    )
}
