package com.acme.transport.terminal.appointment.reservation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the appointment target calculation. */
interface AppointmentTargetModifier {
    fun describe(): String
    fun adjust(item: AppointmentTarget, value: Double): Double
}

data class RoundingAppointmentTargetAdjuster(var step: Double) : AppointmentTargetModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AppointmentTarget, value: Double): Double = roundTo(value, step)
}

data class FlaggedAppointmentTargetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AppointmentTargetModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AppointmentTarget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentAppointmentTargetAdjuster(var percent: Double) : AppointmentTargetModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AppointmentTarget, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the appointment target steps in order and keeps a trace of each. */
data class AppointmentTargetEngine(
    var steps: MutableList<AppointmentTargetModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AppointmentTarget): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AppointmentTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAppointmentTargetEngine(flags: Registry): AppointmentTargetEngine {
    return AppointmentTargetEngine(
        steps = mutableListOf<AppointmentTargetModifier>(
            RoundingAppointmentTargetAdjuster(0.5),
            FlaggedAppointmentTargetAdjuster(flags, "appointment-target-lenient", -7.2),
            PercentAppointmentTargetAdjuster(20.7),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 54051.2,
    )
}
