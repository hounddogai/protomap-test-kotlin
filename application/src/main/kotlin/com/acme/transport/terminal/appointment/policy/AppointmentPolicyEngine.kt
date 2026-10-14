package com.acme.transport.terminal.appointment.policy

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the appointment policy calculation. */
interface AppointmentPolicyAdjuster {
    fun describe(): String
    fun adjust(item: AppointmentPolicy, value: Double): Double
}

data class CapAppointmentPolicyAdjuster(var cap: Double) : AppointmentPolicyAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AppointmentPolicy, value: Double): Double = minOf(value, cap)
}

data class RoundingAppointmentPolicyAdjuster(var step: Double) : AppointmentPolicyAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AppointmentPolicy, value: Double): Double = roundTo(value, step)
}

data class PercentAppointmentPolicyAdjuster(var percent: Double) : AppointmentPolicyAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AppointmentPolicy, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedAppointmentPolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AppointmentPolicyAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AppointmentPolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the appointment policy steps in order and keeps a trace of each. */
data class AppointmentPolicyEngine(
    var steps: MutableList<AppointmentPolicyAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AppointmentPolicy): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AppointmentPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAppointmentPolicyEngine(flags: Registry): AppointmentPolicyEngine {
    return AppointmentPolicyEngine(
        steps = mutableListOf<AppointmentPolicyAdjuster>(
            CapAppointmentPolicyAdjuster(5652.0),
            RoundingAppointmentPolicyAdjuster(5.0),
            PercentAppointmentPolicyAdjuster(18.6),
            FlaggedAppointmentPolicyAdjuster(flags, "appointment-policy-capped", -2.3),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 10900.0,
    )
}
