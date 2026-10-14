package com.acme.transport.terminal.appointment.threshold

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the appointment threshold calculation. */
interface AppointmentThresholdAdjuster {
    fun describe(): String
    fun adjust(item: AppointmentThreshold, value: Double): Double
}

data class AppointmentThresholdLimitOffsetAdjuster(var weight: Double) : AppointmentThresholdAdjuster {
    override fun describe(): String = "plus limit times $weight"

    override fun adjust(item: AppointmentThreshold, value: Double): Double = value + item.limit.toDouble() * weight
}

data class FlaggedAppointmentThresholdAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AppointmentThresholdAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AppointmentThreshold, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CountryAppointmentThresholdAdjuster(
    var factors: MutableMap<String, Double>,
) : AppointmentThresholdAdjuster {
    override fun describe(): String = "by country"

    override fun adjust(item: AppointmentThreshold, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingAppointmentThresholdAdjuster(var step: Double) : AppointmentThresholdAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AppointmentThreshold, value: Double): Double = roundTo(value, step)
}

data class CapAppointmentThresholdAdjuster(var cap: Double) : AppointmentThresholdAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AppointmentThreshold, value: Double): Double = minOf(value, cap)
}

data class TieredAppointmentThresholdAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AppointmentThresholdAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AppointmentThreshold, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the appointment threshold steps in order and keeps a trace of each. */
data class AppointmentThresholdEngine(
    var steps: MutableList<AppointmentThresholdAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AppointmentThreshold): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AppointmentThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAppointmentThresholdEngine(flags: Registry): AppointmentThresholdEngine {
    return AppointmentThresholdEngine(
        steps = mutableListOf<AppointmentThresholdAdjuster>(
            AppointmentThresholdLimitOffsetAdjuster(1.56),
            FlaggedAppointmentThresholdAdjuster(flags, "appointment-threshold-legacy", -2.0),
            CountryAppointmentThresholdAdjuster(mutableMapOf<String, Double>("south" to 1.22)),
            RoundingAppointmentThresholdAdjuster(1.0),
            CapAppointmentThresholdAdjuster(45463.0),
            TieredAppointmentThresholdAdjuster(
                mutableListOf<Double>(501.0, 1849.0, 7543.0, 7622.0),
                mutableListOf<Double>(1.03, 1.03, 1.2, 0.88),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 62120.4,
    )
}
