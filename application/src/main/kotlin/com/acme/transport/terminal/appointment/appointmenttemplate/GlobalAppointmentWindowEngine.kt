package com.acme.transport.terminal.appointment.appointmenttemplate

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the global appointment window calculation. */
interface GlobalAppointmentWindowStep {
    fun describe(): String
    fun adjust(item: GlobalAppointmentWindow, value: Double): Double
}

data class CapGlobalAppointmentWindowAdjuster(var cap: Double) : GlobalAppointmentWindowStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalAppointmentWindow, value: Double): Double = minOf(value, cap)
}

data class TitleGlobalAppointmentWindowAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalAppointmentWindowStep {
    override fun describe(): String = "by title"

    override fun adjust(item: GlobalAppointmentWindow, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingGlobalAppointmentWindowAdjuster(var step: Double) : GlobalAppointmentWindowStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalAppointmentWindow, value: Double): Double = roundTo(value, step)
}

data class FlaggedGlobalAppointmentWindowAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalAppointmentWindowStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalAppointmentWindow, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumGlobalAppointmentWindowAdjuster(var minimum: Double) : GlobalAppointmentWindowStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalAppointmentWindow, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredGlobalAppointmentWindowAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalAppointmentWindowStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalAppointmentWindow, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the global appointment window steps in order and keeps a trace of each. */
data class GlobalAppointmentWindowEngine(
    var steps: MutableList<GlobalAppointmentWindowStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: GlobalAppointmentWindow): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<GlobalAppointmentWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultGlobalAppointmentWindowEngine(flags: Registry): GlobalAppointmentWindowEngine {
    return GlobalAppointmentWindowEngine(
        steps = mutableListOf<GlobalAppointmentWindowStep>(
            CapGlobalAppointmentWindowAdjuster(38919.0),
            TitleGlobalAppointmentWindowAdjuster(mutableMapOf<String, Double>("east" to 1.14)),
            RoundingGlobalAppointmentWindowAdjuster(5.0),
            FlaggedGlobalAppointmentWindowAdjuster(flags, "global-appointment-window-primary", 0.4),
            MinimumGlobalAppointmentWindowAdjuster(28.6),
            TieredGlobalAppointmentWindowAdjuster(
                mutableListOf<Double>(3284.0, 8505.0, 8747.0, 9471.0),
                mutableListOf<Double>(1.03, 1.06, 0.99, 0.84),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 50153.4,
    )
}
