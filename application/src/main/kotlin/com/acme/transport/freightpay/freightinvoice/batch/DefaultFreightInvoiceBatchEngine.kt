package com.acme.transport.freightpay.freightinvoice.batch

import com.acme.transport.kit.flags.Registry

/** One step of the default freight invoice batch calculation. */
interface DefaultFreightInvoiceBatchModifier {
    fun describe(): String
    fun adjust(item: DefaultFreightInvoiceBatch, value: Double): Double
}

data class PercentDefaultFreightInvoiceBatchAdjuster(var percent: Double) : DefaultFreightInvoiceBatchModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultFreightInvoiceBatch, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedDefaultFreightInvoiceBatchAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultFreightInvoiceBatchModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultFreightInvoiceBatch, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class DefaultFreightInvoiceBatchDistanceKmOffsetAdjuster(
    var weight: Double,
) : DefaultFreightInvoiceBatchModifier {
    override fun describe(): String = "plus distance km times $weight"

    override fun adjust(item: DefaultFreightInvoiceBatch, value: Double): Double = value + item.distanceKm * weight
}

data class MinimumDefaultFreightInvoiceBatchAdjuster(var minimum: Double) : DefaultFreightInvoiceBatchModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultFreightInvoiceBatch, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapDefaultFreightInvoiceBatchAdjuster(var cap: Double) : DefaultFreightInvoiceBatchModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultFreightInvoiceBatch, value: Double): Double = minOf(value, cap)
}

/** Runs the default freight invoice batch steps in order and keeps a trace of each. */
data class DefaultFreightInvoiceBatchEngine(
    var steps: MutableList<DefaultFreightInvoiceBatchModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DefaultFreightInvoiceBatch): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultFreightInvoiceBatchModifier = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultFreightInvoiceBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDefaultFreightInvoiceBatchEngine(flags: Registry): DefaultFreightInvoiceBatchEngine {
    return DefaultFreightInvoiceBatchEngine(
        steps = mutableListOf<DefaultFreightInvoiceBatchModifier>(
            PercentDefaultFreightInvoiceBatchAdjuster(-23.5),
            FlaggedDefaultFreightInvoiceBatchAdjuster(flags, "default-freight-invoice-batch-strict", 2.0),
            DefaultFreightInvoiceBatchDistanceKmOffsetAdjuster(0.39),
            MinimumDefaultFreightInvoiceBatchAdjuster(8.0),
            CapDefaultFreightInvoiceBatchAdjuster(45927.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 56299.3,
    )
}
