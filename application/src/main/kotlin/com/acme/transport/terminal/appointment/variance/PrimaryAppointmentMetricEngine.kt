package com.acme.transport.terminal.appointment.variance

import com.acme.transport.kit.flags.Registry

/** One step of the primary appointment metric calculation. */
interface PrimaryAppointmentMetricModifier {
    fun describe(): String
    fun adjust(item: PrimaryAppointmentMetric, value: Double): Double
}

data class FlaggedPrimaryAppointmentMetricAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryAppointmentMetricModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryAppointmentMetric, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PrimaryAppointmentMetricDistanceKmOffsetAdjuster(
    var weight: Double,
) : PrimaryAppointmentMetricModifier {
    override fun describe(): String = "plus distance km times $weight"

    override fun adjust(item: PrimaryAppointmentMetric, value: Double): Double = value + item.distanceKm * weight
}

data class PercentPrimaryAppointmentMetricAdjuster(var percent: Double) : PrimaryAppointmentMetricModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryAppointmentMetric, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumPrimaryAppointmentMetricAdjuster(var minimum: Double) : PrimaryAppointmentMetricModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryAppointmentMetric, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredPrimaryAppointmentMetricAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryAppointmentMetricModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryAppointmentMetric, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the primary appointment metric steps in order and keeps a trace of each. */
data class PrimaryAppointmentMetricEngine(
    var steps: MutableList<PrimaryAppointmentMetricModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PrimaryAppointmentMetric): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryAppointmentMetricModifier = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryAppointmentMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPrimaryAppointmentMetricEngine(flags: Registry): PrimaryAppointmentMetricEngine {
    return PrimaryAppointmentMetricEngine(
        steps = mutableListOf<PrimaryAppointmentMetricModifier>(
            FlaggedPrimaryAppointmentMetricAdjuster(flags, "primary-appointment-metric-weighted", -2.0),
            PrimaryAppointmentMetricDistanceKmOffsetAdjuster(0.96),
            PercentPrimaryAppointmentMetricAdjuster(4.0),
            MinimumPrimaryAppointmentMetricAdjuster(36.1),
            TieredPrimaryAppointmentMetricAdjuster(
                mutableListOf<Double>(4920.0, 5453.0, 7441.0),
                mutableListOf<Double>(0.9, 0.81, 1.18),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 60235.2,
    )
}
