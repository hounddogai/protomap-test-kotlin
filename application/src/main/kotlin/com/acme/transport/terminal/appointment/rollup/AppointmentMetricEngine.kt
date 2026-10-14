package com.acme.transport.terminal.appointment.rollup

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the appointment metric calculation. */
interface AppointmentMetricStep {
    fun describe(): String
    fun adjust(item: AppointmentMetric, value: Double): Double
}

data class CapAppointmentMetricAdjuster(var cap: Double) : AppointmentMetricStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AppointmentMetric, value: Double): Double = minOf(value, cap)
}

data class RoundingAppointmentMetricAdjuster(var step: Double) : AppointmentMetricStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AppointmentMetric, value: Double): Double = roundTo(value, step)
}

data class FlaggedAppointmentMetricAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AppointmentMetricStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AppointmentMetric, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredAppointmentMetricAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AppointmentMetricStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AppointmentMetric, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentAppointmentMetricAdjuster(var percent: Double) : AppointmentMetricStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AppointmentMetric, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the appointment metric steps in order and keeps a trace of each. */
data class AppointmentMetricEngine(
    var steps: MutableList<AppointmentMetricStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AppointmentMetric): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AppointmentMetricStep = steps[index]
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

    fun evaluateAll(items: MutableList<AppointmentMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAppointmentMetricEngine(flags: Registry): AppointmentMetricEngine {
    return AppointmentMetricEngine(
        steps = mutableListOf<AppointmentMetricStep>(
            CapAppointmentMetricAdjuster(18683.0),
            RoundingAppointmentMetricAdjuster(5.0),
            FlaggedAppointmentMetricAdjuster(flags, "appointment-metric-primary", -8.0),
            TieredAppointmentMetricAdjuster(
                mutableListOf<Double>(411.0, 7129.0, 9235.0, 9396.0),
                mutableListOf<Double>(1.01, 1.08, 0.82, 1.09),
            ),
            PercentAppointmentMetricAdjuster(2.3),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 19351.7,
    )
}
