package com.acme.transport.terminal.appointment.exception

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the primary appointment quota calculation. */
interface PrimaryAppointmentQuotaFactor {
    fun describe(): String
    fun adjust(item: PrimaryAppointmentQuota, value: Double): Double
}

data class MinimumPrimaryAppointmentQuotaAdjuster(var minimum: Double) : PrimaryAppointmentQuotaFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryAppointmentQuota, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedPrimaryAppointmentQuotaAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryAppointmentQuotaFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryAppointmentQuota, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentPrimaryAppointmentQuotaAdjuster(var percent: Double) : PrimaryAppointmentQuotaFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryAppointmentQuota, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class PrimaryAppointmentQuotaRetriesOffsetAdjuster(var weight: Double) : PrimaryAppointmentQuotaFactor {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: PrimaryAppointmentQuota, value: Double): Double {
        return value + item.retries.toDouble() * weight
    }
}

data class RoundingPrimaryAppointmentQuotaAdjuster(var step: Double) : PrimaryAppointmentQuotaFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryAppointmentQuota, value: Double): Double = roundTo(value, step)
}

/** Runs the primary appointment quota steps in order and keeps a trace of each. */
data class PrimaryAppointmentQuotaEngine(
    var steps: MutableList<PrimaryAppointmentQuotaFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PrimaryAppointmentQuota): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryAppointmentQuotaFactor = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryAppointmentQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPrimaryAppointmentQuotaEngine(flags: Registry): PrimaryAppointmentQuotaEngine {
    return PrimaryAppointmentQuotaEngine(
        steps = mutableListOf<PrimaryAppointmentQuotaFactor>(
            MinimumPrimaryAppointmentQuotaAdjuster(37.0),
            FlaggedPrimaryAppointmentQuotaAdjuster(flags, "primary-appointment-quota-pending", 8.8),
            PercentPrimaryAppointmentQuotaAdjuster(-12.8),
            PrimaryAppointmentQuotaRetriesOffsetAdjuster(1.16),
            RoundingPrimaryAppointmentQuotaAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 69976.3,
    )
}
