package com.acme.transport.terminal.appointment.rollingappointmentvariance

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the strict appointment variance calculation. */
interface StrictAppointmentVarianceFactor {
    fun describe(): String
    fun adjust(item: StrictAppointmentVariance, value: Double): Double
}

data class CountryStrictAppointmentVarianceAdjuster(
    var factors: MutableMap<String, Double>,
) : StrictAppointmentVarianceFactor {
    override fun describe(): String = "by country"

    override fun adjust(item: StrictAppointmentVariance, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapStrictAppointmentVarianceAdjuster(var cap: Double) : StrictAppointmentVarianceFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictAppointmentVariance, value: Double): Double = minOf(value, cap)
}

data class MinimumStrictAppointmentVarianceAdjuster(var minimum: Double) : StrictAppointmentVarianceFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictAppointmentVariance, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedStrictAppointmentVarianceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictAppointmentVarianceFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictAppointmentVariance, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingStrictAppointmentVarianceAdjuster(var step: Double) : StrictAppointmentVarianceFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictAppointmentVariance, value: Double): Double = roundTo(value, step)
}

/** Runs the strict appointment variance steps in order and keeps a trace of each. */
data class StrictAppointmentVarianceEngine(
    var steps: MutableList<StrictAppointmentVarianceFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: StrictAppointmentVariance): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictAppointmentVarianceFactor = steps[index]
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

    fun evaluateAll(items: MutableList<StrictAppointmentVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultStrictAppointmentVarianceEngine(flags: Registry): StrictAppointmentVarianceEngine {
    return StrictAppointmentVarianceEngine(
        steps = mutableListOf<StrictAppointmentVarianceFactor>(
            CountryStrictAppointmentVarianceAdjuster(mutableMapOf<String, Double>("central" to 0.97)),
            CapStrictAppointmentVarianceAdjuster(40724.0),
            MinimumStrictAppointmentVarianceAdjuster(4.4),
            FlaggedStrictAppointmentVarianceAdjuster(flags, "strict-appointment-variance-adaptive", -9.5),
            RoundingStrictAppointmentVarianceAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 70201.6,
    )
}
