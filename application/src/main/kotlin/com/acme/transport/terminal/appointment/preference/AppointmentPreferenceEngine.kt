package com.acme.transport.terminal.appointment.preference

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the appointment preference calculation. */
interface AppointmentPreferenceModifier {
    fun describe(): String
    fun adjust(item: AppointmentPreference, value: Double): Double
}

data class CapAppointmentPreferenceAdjuster(var cap: Double) : AppointmentPreferenceModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AppointmentPreference, value: Double): Double = minOf(value, cap)
}

data class ChannelAppointmentPreferenceAdjuster(
    var factors: MutableMap<String, Double>,
) : AppointmentPreferenceModifier {
    override fun describe(): String = "by channel"

    override fun adjust(item: AppointmentPreference, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedAppointmentPreferenceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AppointmentPreferenceModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AppointmentPreference, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentAppointmentPreferenceAdjuster(var percent: Double) : AppointmentPreferenceModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AppointmentPreference, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the appointment preference steps in order and keeps a trace of each. */
data class AppointmentPreferenceEngine(
    var steps: MutableList<AppointmentPreferenceModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AppointmentPreference): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AppointmentPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAppointmentPreferenceEngine(flags: Registry): AppointmentPreferenceEngine {
    return AppointmentPreferenceEngine(
        steps = mutableListOf<AppointmentPreferenceModifier>(
            CapAppointmentPreferenceAdjuster(12059.0),
            ChannelAppointmentPreferenceAdjuster(
                mutableMapOf<String, Double>("central" to 1.23, "west" to 1.09),
            ),
            FlaggedAppointmentPreferenceAdjuster(flags, "appointment-preference-rolling", 5.3),
            PercentAppointmentPreferenceAdjuster(-21.1),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 12074.9,
    )
}
