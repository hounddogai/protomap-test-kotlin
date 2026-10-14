package com.acme.transport.freightpay.freightinvoice.manifest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the freight invoice draft calculation. */
interface FreightInvoiceDraftAdjuster {
    fun describe(): String
    fun adjust(item: FreightInvoiceDraft, value: Double): Double
}

data class PercentFreightInvoiceDraftAdjuster(var percent: Double) : FreightInvoiceDraftAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FreightInvoiceDraft, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapFreightInvoiceDraftAdjuster(var cap: Double) : FreightInvoiceDraftAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FreightInvoiceDraft, value: Double): Double = minOf(value, cap)
}

data class RoundingFreightInvoiceDraftAdjuster(var step: Double) : FreightInvoiceDraftAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FreightInvoiceDraft, value: Double): Double = roundTo(value, step)
}

data class FreightInvoiceDraftAttemptsOffsetAdjuster(var weight: Double) : FreightInvoiceDraftAdjuster {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: FreightInvoiceDraft, value: Double): Double = value + item.attempts.toDouble() * weight
}

data class LocaleFreightInvoiceDraftAdjuster(
    var factors: MutableMap<String, Double>,
) : FreightInvoiceDraftAdjuster {
    override fun describe(): String = "by locale"

    override fun adjust(item: FreightInvoiceDraft, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedFreightInvoiceDraftAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FreightInvoiceDraftAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FreightInvoiceDraft, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the freight invoice draft steps in order and keeps a trace of each. */
data class FreightInvoiceDraftEngine(
    var steps: MutableList<FreightInvoiceDraftAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: FreightInvoiceDraft): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FreightInvoiceDraftAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<FreightInvoiceDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultFreightInvoiceDraftEngine(flags: Registry): FreightInvoiceDraftEngine {
    return FreightInvoiceDraftEngine(
        steps = mutableListOf<FreightInvoiceDraftAdjuster>(
            PercentFreightInvoiceDraftAdjuster(-22.8),
            CapFreightInvoiceDraftAdjuster(40413.0),
            RoundingFreightInvoiceDraftAdjuster(0.1),
            FreightInvoiceDraftAttemptsOffsetAdjuster(1.11),
            LocaleFreightInvoiceDraftAdjuster(
                mutableMapOf<String, Double>("west" to 0.85, "south" to 1.37, "north" to 0.93),
            ),
            FlaggedFreightInvoiceDraftAdjuster(flags, "freight-invoice-draft-capped", 0.4),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 65155.6,
    )
}
