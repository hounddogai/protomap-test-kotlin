package com.acme.transport.freightpay.freightinvoice.schedule

import com.acme.transport.kit.flags.Registry

/** One step of the freight invoice schedule calculation. */
interface FreightInvoiceScheduleFactor {
    fun describe(): String
    fun adjust(item: FreightInvoiceSchedule, value: Double): Double
}

data class FlaggedFreightInvoiceScheduleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FreightInvoiceScheduleFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FreightInvoiceSchedule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LocaleFreightInvoiceScheduleAdjuster(
    var factors: MutableMap<String, Double>,
) : FreightInvoiceScheduleFactor {
    override fun describe(): String = "by locale"

    override fun adjust(item: FreightInvoiceSchedule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumFreightInvoiceScheduleAdjuster(var minimum: Double) : FreightInvoiceScheduleFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FreightInvoiceSchedule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the freight invoice schedule steps in order and keeps a trace of each. */
data class FreightInvoiceScheduleEngine(
    var steps: MutableList<FreightInvoiceScheduleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: FreightInvoiceSchedule): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FreightInvoiceScheduleFactor = steps[index]
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

    fun evaluateAll(items: MutableList<FreightInvoiceSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultFreightInvoiceScheduleEngine(flags: Registry): FreightInvoiceScheduleEngine {
    return FreightInvoiceScheduleEngine(
        steps = mutableListOf<FreightInvoiceScheduleFactor>(
            FlaggedFreightInvoiceScheduleAdjuster(flags, "freight-invoice-schedule-capped", -1.5),
            LocaleFreightInvoiceScheduleAdjuster(
                mutableMapOf<String, Double>("south" to 0.94, "central" to 1.35, "west" to 0.97),
            ),
            MinimumFreightInvoiceScheduleAdjuster(11.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 8591.6,
    )
}
