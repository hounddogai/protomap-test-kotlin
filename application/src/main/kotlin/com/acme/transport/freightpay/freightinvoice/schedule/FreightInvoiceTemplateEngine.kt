package com.acme.transport.freightpay.freightinvoice.schedule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the freight invoice template calculation. */
interface FreightInvoiceTemplateAdjuster {
    fun describe(): String
    fun adjust(item: FreightInvoiceTemplate, value: Double): Double
}

data class FreightInvoiceTemplateSequenceOffsetAdjuster(var weight: Double) : FreightInvoiceTemplateAdjuster {
    override fun describe(): String = "plus sequence times $weight"

    override fun adjust(item: FreightInvoiceTemplate, value: Double): Double {
        return value + item.sequence.toDouble() * weight
    }
}

data class RoundingFreightInvoiceTemplateAdjuster(var step: Double) : FreightInvoiceTemplateAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FreightInvoiceTemplate, value: Double): Double = roundTo(value, step)
}

data class FlaggedFreightInvoiceTemplateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FreightInvoiceTemplateAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FreightInvoiceTemplate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the freight invoice template steps in order and keeps a trace of each. */
data class FreightInvoiceTemplateEngine(
    var steps: MutableList<FreightInvoiceTemplateAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: FreightInvoiceTemplate): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FreightInvoiceTemplateAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<FreightInvoiceTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultFreightInvoiceTemplateEngine(flags: Registry): FreightInvoiceTemplateEngine {
    return FreightInvoiceTemplateEngine(
        steps = mutableListOf<FreightInvoiceTemplateAdjuster>(
            FreightInvoiceTemplateSequenceOffsetAdjuster(0.5),
            RoundingFreightInvoiceTemplateAdjuster(0.25),
            FlaggedFreightInvoiceTemplateAdjuster(flags, "freight-invoice-template-primary", -9.4),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 92123.2,
    )
}
