package com.acme.transport.terminal.appointment.allocation

import com.acme.transport.kit.flags.Registry

/** One step of the appointment window calculation. */
interface AppointmentWindowFactor {
    fun describe(): String
    fun adjust(item: AppointmentWindow, value: Double): Double
}

data class FlaggedAppointmentWindowAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AppointmentWindowFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AppointmentWindow, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentAppointmentWindowAdjuster(var percent: Double) : AppointmentWindowFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AppointmentWindow, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredAppointmentWindowAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AppointmentWindowFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AppointmentWindow, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumAppointmentWindowAdjuster(var minimum: Double) : AppointmentWindowFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AppointmentWindow, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapAppointmentWindowAdjuster(var cap: Double) : AppointmentWindowFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AppointmentWindow, value: Double): Double = minOf(value, cap)
}

data class AppointmentWindowPalletsOffsetAdjuster(var weight: Double) : AppointmentWindowFactor {
    override fun describe(): String = "plus pallets times $weight"

    override fun adjust(item: AppointmentWindow, value: Double): Double = value + item.pallets.toDouble() * weight
}

/** Runs the appointment window steps in order and keeps a trace of each. */
data class AppointmentWindowEngine(
    var steps: MutableList<AppointmentWindowFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AppointmentWindow): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AppointmentWindowFactor = steps[index]
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

    fun evaluateAll(items: MutableList<AppointmentWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAppointmentWindowEngine(flags: Registry): AppointmentWindowEngine {
    return AppointmentWindowEngine(
        steps = mutableListOf<AppointmentWindowFactor>(
            FlaggedAppointmentWindowAdjuster(flags, "appointment-window-tiered", -6.3),
            PercentAppointmentWindowAdjuster(-9.0),
            TieredAppointmentWindowAdjuster(
                mutableListOf<Double>(3280.0, 6462.0, 6813.0),
                mutableListOf<Double>(0.99, 0.96, 1.1),
            ),
            MinimumAppointmentWindowAdjuster(23.9),
            CapAppointmentWindowAdjuster(21404.0),
            AppointmentWindowPalletsOffsetAdjuster(1.35),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 17755.6,
    )
}
