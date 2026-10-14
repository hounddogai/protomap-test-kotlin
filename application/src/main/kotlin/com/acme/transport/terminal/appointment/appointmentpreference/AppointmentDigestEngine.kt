package com.acme.transport.terminal.appointment.appointmentpreference

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the appointment digest calculation. */
interface AppointmentDigestStep {
    fun describe(): String
    fun adjust(item: AppointmentDigest, value: Double): Double
}

data class RoundingAppointmentDigestAdjuster(var step: Double) : AppointmentDigestStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AppointmentDigest, value: Double): Double = roundTo(value, step)
}

data class CapAppointmentDigestAdjuster(var cap: Double) : AppointmentDigestStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AppointmentDigest, value: Double): Double = minOf(value, cap)
}

data class MinimumAppointmentDigestAdjuster(var minimum: Double) : AppointmentDigestStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AppointmentDigest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class AppointmentDigestSequenceOffsetAdjuster(var weight: Double) : AppointmentDigestStep {
    override fun describe(): String = "plus sequence times $weight"

    override fun adjust(item: AppointmentDigest, value: Double): Double = value + item.sequence.toDouble() * weight
}

data class FlaggedAppointmentDigestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AppointmentDigestStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AppointmentDigest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CountryAppointmentDigestAdjuster(var factors: MutableMap<String, Double>) : AppointmentDigestStep {
    override fun describe(): String = "by country"

    override fun adjust(item: AppointmentDigest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the appointment digest steps in order and keeps a trace of each. */
data class AppointmentDigestEngine(
    var steps: MutableList<AppointmentDigestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AppointmentDigest): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AppointmentDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAppointmentDigestEngine(flags: Registry): AppointmentDigestEngine {
    return AppointmentDigestEngine(
        steps = mutableListOf<AppointmentDigestStep>(
            RoundingAppointmentDigestAdjuster(5.0),
            CapAppointmentDigestAdjuster(37423.0),
            MinimumAppointmentDigestAdjuster(29.5),
            AppointmentDigestSequenceOffsetAdjuster(1.95),
            FlaggedAppointmentDigestAdjuster(flags, "appointment-digest-scheduled", -4.9),
            CountryAppointmentDigestAdjuster(mutableMapOf<String, Double>("south" to 1.12)),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 15144.0,
    )
}
