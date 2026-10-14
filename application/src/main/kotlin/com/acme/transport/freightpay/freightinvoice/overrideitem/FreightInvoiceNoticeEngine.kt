package com.acme.transport.freightpay.freightinvoice.overrideitem

import com.acme.transport.kit.flags.Registry

/** One step of the freight invoice notice calculation. */
interface FreightInvoiceNoticeModifier {
    fun describe(): String
    fun adjust(item: FreightInvoiceNotice, value: Double): Double
}

data class FlaggedFreightInvoiceNoticeAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FreightInvoiceNoticeModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FreightInvoiceNotice, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumFreightInvoiceNoticeAdjuster(var minimum: Double) : FreightInvoiceNoticeModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FreightInvoiceNotice, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentFreightInvoiceNoticeAdjuster(var percent: Double) : FreightInvoiceNoticeModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FreightInvoiceNotice, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the freight invoice notice steps in order and keeps a trace of each. */
data class FreightInvoiceNoticeEngine(
    var steps: MutableList<FreightInvoiceNoticeModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: FreightInvoiceNotice): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FreightInvoiceNoticeModifier = steps[index]
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

    fun evaluateAll(items: MutableList<FreightInvoiceNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultFreightInvoiceNoticeEngine(flags: Registry): FreightInvoiceNoticeEngine {
    return FreightInvoiceNoticeEngine(
        steps = mutableListOf<FreightInvoiceNoticeModifier>(
            FlaggedFreightInvoiceNoticeAdjuster(flags, "freight-invoice-notice-legacy", -5.4),
            MinimumFreightInvoiceNoticeAdjuster(5.5),
            PercentFreightInvoiceNoticeAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 61414.4,
    )
}
