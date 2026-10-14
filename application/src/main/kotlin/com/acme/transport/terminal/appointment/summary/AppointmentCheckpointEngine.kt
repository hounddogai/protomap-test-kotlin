package com.acme.transport.terminal.appointment.summary

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the appointment checkpoint calculation. */
interface AppointmentCheckpointStep {
    fun describe(): String
    fun adjust(item: AppointmentCheckpoint, value: Double): Double
}

data class FlaggedAppointmentCheckpointAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AppointmentCheckpointStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AppointmentCheckpoint, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapAppointmentCheckpointAdjuster(var cap: Double) : AppointmentCheckpointStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AppointmentCheckpoint, value: Double): Double = minOf(value, cap)
}

data class RoundingAppointmentCheckpointAdjuster(var step: Double) : AppointmentCheckpointStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AppointmentCheckpoint, value: Double): Double = roundTo(value, step)
}

data class AppointmentCheckpointMinimumOffsetAdjuster(var weight: Double) : AppointmentCheckpointStep {
    override fun describe(): String = "plus minimum times $weight"

    override fun adjust(item: AppointmentCheckpoint, value: Double): Double = value + item.minimum * weight
}

data class PercentAppointmentCheckpointAdjuster(var percent: Double) : AppointmentCheckpointStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AppointmentCheckpoint, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumAppointmentCheckpointAdjuster(var minimum: Double) : AppointmentCheckpointStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AppointmentCheckpoint, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the appointment checkpoint steps in order and keeps a trace of each. */
data class AppointmentCheckpointEngine(
    var steps: MutableList<AppointmentCheckpointStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AppointmentCheckpoint): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AppointmentCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAppointmentCheckpointEngine(flags: Registry): AppointmentCheckpointEngine {
    return AppointmentCheckpointEngine(
        steps = mutableListOf<AppointmentCheckpointStep>(
            FlaggedAppointmentCheckpointAdjuster(flags, "appointment-checkpoint-scheduled", -0.5),
            CapAppointmentCheckpointAdjuster(13135.0),
            RoundingAppointmentCheckpointAdjuster(0.1),
            AppointmentCheckpointMinimumOffsetAdjuster(1.4),
            PercentAppointmentCheckpointAdjuster(-21.2),
            MinimumAppointmentCheckpointAdjuster(25.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 3234.4,
    )
}
