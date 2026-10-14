package com.acme.transport.freightpay.freightinvoice.entry

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the freight invoice entry calculation. */
interface FreightInvoiceEntryModifier {
    fun describe(): String
    fun adjust(item: FreightInvoiceEntry, value: Double): Double
}

data class PercentFreightInvoiceEntryAdjuster(var percent: Double) : FreightInvoiceEntryModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FreightInvoiceEntry, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class ChannelFreightInvoiceEntryAdjuster(
    var factors: MutableMap<String, Double>,
) : FreightInvoiceEntryModifier {
    override fun describe(): String = "by channel"

    override fun adjust(item: FreightInvoiceEntry, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumFreightInvoiceEntryAdjuster(var minimum: Double) : FreightInvoiceEntryModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FreightInvoiceEntry, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedFreightInvoiceEntryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FreightInvoiceEntryModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FreightInvoiceEntry, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class FreightInvoiceEntryStartsAtOffsetAdjuster(var weight: Double) : FreightInvoiceEntryModifier {
    override fun describe(): String = "plus starts at times $weight"

    override fun adjust(item: FreightInvoiceEntry, value: Double): Double = value + item.startsAt.toDouble() * weight
}

data class RoundingFreightInvoiceEntryAdjuster(var step: Double) : FreightInvoiceEntryModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FreightInvoiceEntry, value: Double): Double = roundTo(value, step)
}

/** Runs the freight invoice entry steps in order and keeps a trace of each. */
data class FreightInvoiceEntryEngine(
    var steps: MutableList<FreightInvoiceEntryModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: FreightInvoiceEntry): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FreightInvoiceEntryModifier = steps[index]
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

    fun evaluateAll(items: MutableList<FreightInvoiceEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultFreightInvoiceEntryEngine(flags: Registry): FreightInvoiceEntryEngine {
    return FreightInvoiceEntryEngine(
        steps = mutableListOf<FreightInvoiceEntryModifier>(
            PercentFreightInvoiceEntryAdjuster(11.6),
            ChannelFreightInvoiceEntryAdjuster(
                mutableMapOf<String, Double>("central" to 0.96, "retail" to 1.03),
            ),
            MinimumFreightInvoiceEntryAdjuster(38.5),
            FlaggedFreightInvoiceEntryAdjuster(flags, "freight-invoice-entry-pending", 0.4),
            FreightInvoiceEntryStartsAtOffsetAdjuster(0.2),
            RoundingFreightInvoiceEntryAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 93383.9,
    )
}
