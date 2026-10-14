package com.acme.transport.terminal.appointment.appointmentvariance

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the scheduled appointment variance calculation. */
interface ScheduledAppointmentVarianceStep {
    fun describe(): String
    fun adjust(item: ScheduledAppointmentVariance, value: Double): Double
}

data class TieredScheduledAppointmentVarianceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledAppointmentVarianceStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledAppointmentVariance, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentScheduledAppointmentVarianceAdjuster(var percent: Double) : ScheduledAppointmentVarianceStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledAppointmentVariance, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TitleScheduledAppointmentVarianceAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledAppointmentVarianceStep {
    override fun describe(): String = "by title"

    override fun adjust(item: ScheduledAppointmentVariance, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class ScheduledAppointmentVarianceCapacityOffsetAdjuster(
    var weight: Double,
) : ScheduledAppointmentVarianceStep {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: ScheduledAppointmentVariance, value: Double): Double {
        return value + item.capacity.toDouble() * weight
    }
}

data class FlaggedScheduledAppointmentVarianceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledAppointmentVarianceStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledAppointmentVariance, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingScheduledAppointmentVarianceAdjuster(var step: Double) : ScheduledAppointmentVarianceStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledAppointmentVariance, value: Double): Double = roundTo(value, step)
}

/** Runs the scheduled appointment variance steps in order and keeps a trace of each. */
data class ScheduledAppointmentVarianceEngine(
    var steps: MutableList<ScheduledAppointmentVarianceStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ScheduledAppointmentVariance): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ScheduledAppointmentVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultScheduledAppointmentVarianceEngine(flags: Registry): ScheduledAppointmentVarianceEngine {
    return ScheduledAppointmentVarianceEngine(
        steps = mutableListOf<ScheduledAppointmentVarianceStep>(
            TieredScheduledAppointmentVarianceAdjuster(
                mutableListOf<Double>(2987.0, 5358.0),
                mutableListOf<Double>(0.92, 1.02),
            ),
            PercentScheduledAppointmentVarianceAdjuster(-10.1),
            TitleScheduledAppointmentVarianceAdjuster(mutableMapOf<String, Double>("west" to 1.08)),
            ScheduledAppointmentVarianceCapacityOffsetAdjuster(1.67),
            FlaggedScheduledAppointmentVarianceAdjuster(flags, "scheduled-appointment-variance-weighted", -7.9),
            RoundingScheduledAppointmentVarianceAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 63379.2,
    )
}
