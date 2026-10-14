package com.acme.transport.terminal.appointment.bundle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the appointment quota calculation. */
interface AppointmentQuotaModifier {
    fun describe(): String
    fun adjust(item: AppointmentQuota, value: Double): Double
}

data class MinimumAppointmentQuotaAdjuster(var minimum: Double) : AppointmentQuotaModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AppointmentQuota, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedAppointmentQuotaAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AppointmentQuotaModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AppointmentQuota, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredAppointmentQuotaAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AppointmentQuotaModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AppointmentQuota, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentAppointmentQuotaAdjuster(var percent: Double) : AppointmentQuotaModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AppointmentQuota, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the appointment quota steps in order and keeps a trace of each. */
data class AppointmentQuotaEngine(
    var steps: MutableList<AppointmentQuotaModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AppointmentQuota): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AppointmentQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAppointmentQuotaEngine(flags: Registry): AppointmentQuotaEngine {
    return AppointmentQuotaEngine(
        steps = mutableListOf<AppointmentQuotaModifier>(
            MinimumAppointmentQuotaAdjuster(2.6),
            FlaggedAppointmentQuotaAdjuster(flags, "appointment-quota-legacy", -2.4),
            TieredAppointmentQuotaAdjuster(
                mutableListOf<Double>(3513.0, 5323.0, 5719.0),
                mutableListOf<Double>(0.84, 0.82, 1.19),
            ),
            PercentAppointmentQuotaAdjuster(-20.7),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 8099.0,
    )
}
