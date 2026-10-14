package com.acme.transport.freightpay.freightinvoice.allocation

import com.acme.transport.kit.flags.Registry

/** One step of the pending freight invoice schedule calculation. */
interface PendingFreightInvoiceScheduleStep {
    fun describe(): String
    fun adjust(item: PendingFreightInvoiceSchedule, value: Double): Double
}

data class PercentPendingFreightInvoiceScheduleAdjuster(
    var percent: Double,
) : PendingFreightInvoiceScheduleStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingFreightInvoiceSchedule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredPendingFreightInvoiceScheduleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingFreightInvoiceScheduleStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingFreightInvoiceSchedule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumPendingFreightInvoiceScheduleAdjuster(
    var minimum: Double,
) : PendingFreightInvoiceScheduleStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingFreightInvoiceSchedule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapPendingFreightInvoiceScheduleAdjuster(var cap: Double) : PendingFreightInvoiceScheduleStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingFreightInvoiceSchedule, value: Double): Double = minOf(value, cap)
}

data class FlaggedPendingFreightInvoiceScheduleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingFreightInvoiceScheduleStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingFreightInvoiceSchedule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the pending freight invoice schedule steps in order and keeps a trace of each. */
data class PendingFreightInvoiceScheduleEngine(
    var steps: MutableList<PendingFreightInvoiceScheduleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PendingFreightInvoiceSchedule): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingFreightInvoiceScheduleStep = steps[index]
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

    fun evaluateAll(items: MutableList<PendingFreightInvoiceSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPendingFreightInvoiceScheduleEngine(flags: Registry): PendingFreightInvoiceScheduleEngine {
    return PendingFreightInvoiceScheduleEngine(
        steps = mutableListOf<PendingFreightInvoiceScheduleStep>(
            PercentPendingFreightInvoiceScheduleAdjuster(-4.4),
            TieredPendingFreightInvoiceScheduleAdjuster(
                mutableListOf<Double>(2309.0, 4911.0, 6840.0, 9710.0),
                mutableListOf<Double>(1.08, 1.02, 1.04, 1.15),
            ),
            MinimumPendingFreightInvoiceScheduleAdjuster(38.5),
            CapPendingFreightInvoiceScheduleAdjuster(5962.0),
            FlaggedPendingFreightInvoiceScheduleAdjuster(flags, "pending-freight-invoice-schedule-tiered", -6.7),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 83985.1,
    )
}
