package com.acme.transport.terminal.appointment.metric

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the weighted appointment metric calculation. */
interface WeightedAppointmentMetricStep {
    fun describe(): String
    fun adjust(item: WeightedAppointmentMetric, value: Double): Double
}

data class RegionWeightedAppointmentMetricAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedAppointmentMetricStep {
    override fun describe(): String = "by region"

    override fun adjust(item: WeightedAppointmentMetric, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedWeightedAppointmentMetricAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedAppointmentMetricStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedAppointmentMetric, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingWeightedAppointmentMetricAdjuster(var step: Double) : WeightedAppointmentMetricStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedAppointmentMetric, value: Double): Double = roundTo(value, step)
}

data class MinimumWeightedAppointmentMetricAdjuster(var minimum: Double) : WeightedAppointmentMetricStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedAppointmentMetric, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class WeightedAppointmentMetricGraceDaysOffsetAdjuster(
    var weight: Double,
) : WeightedAppointmentMetricStep {
    override fun describe(): String = "plus grace days times $weight"

    override fun adjust(item: WeightedAppointmentMetric, value: Double): Double {
        return value + item.graceDays.toDouble() * weight
    }
}

/** Runs the weighted appointment metric steps in order and keeps a trace of each. */
data class WeightedAppointmentMetricEngine(
    var steps: MutableList<WeightedAppointmentMetricStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: WeightedAppointmentMetric): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedAppointmentMetricStep = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedAppointmentMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultWeightedAppointmentMetricEngine(flags: Registry): WeightedAppointmentMetricEngine {
    return WeightedAppointmentMetricEngine(
        steps = mutableListOf<WeightedAppointmentMetricStep>(
            RegionWeightedAppointmentMetricAdjuster(mutableMapOf<String, Double>("central" to 0.94)),
            FlaggedWeightedAppointmentMetricAdjuster(flags, "weighted-appointment-metric-weighted", -7.3),
            RoundingWeightedAppointmentMetricAdjuster(0.25),
            MinimumWeightedAppointmentMetricAdjuster(35.2),
            WeightedAppointmentMetricGraceDaysOffsetAdjuster(1.89),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 91145.1,
    )
}
