package com.acme.transport.freightpay.freightinvoice.group

import com.acme.transport.kit.flags.Registry

/** One step of the freight invoice digest calculation. */
interface FreightInvoiceDigestModifier {
    fun describe(): String
    fun adjust(item: FreightInvoiceDigest, value: Double): Double
}

data class FlaggedFreightInvoiceDigestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FreightInvoiceDigestModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FreightInvoiceDigest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredFreightInvoiceDigestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FreightInvoiceDigestModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FreightInvoiceDigest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentFreightInvoiceDigestAdjuster(var percent: Double) : FreightInvoiceDigestModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FreightInvoiceDigest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapFreightInvoiceDigestAdjuster(var cap: Double) : FreightInvoiceDigestModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FreightInvoiceDigest, value: Double): Double = minOf(value, cap)
}

data class FreightInvoiceDigestRatioOffsetAdjuster(var weight: Double) : FreightInvoiceDigestModifier {
    override fun describe(): String = "plus ratio times $weight"

    override fun adjust(item: FreightInvoiceDigest, value: Double): Double = value + item.ratio * weight
}

/** Runs the freight invoice digest steps in order and keeps a trace of each. */
data class FreightInvoiceDigestEngine(
    var steps: MutableList<FreightInvoiceDigestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: FreightInvoiceDigest): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FreightInvoiceDigestModifier = steps[index]
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

    fun evaluateAll(items: MutableList<FreightInvoiceDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultFreightInvoiceDigestEngine(flags: Registry): FreightInvoiceDigestEngine {
    return FreightInvoiceDigestEngine(
        steps = mutableListOf<FreightInvoiceDigestModifier>(
            FlaggedFreightInvoiceDigestAdjuster(flags, "freight-invoice-digest-lenient", 3.6),
            TieredFreightInvoiceDigestAdjuster(
                mutableListOf<Double>(1361.0, 1658.0, 8538.0),
                mutableListOf<Double>(0.84, 1.02, 0.84),
            ),
            PercentFreightInvoiceDigestAdjuster(22.7),
            CapFreightInvoiceDigestAdjuster(41563.0),
            FreightInvoiceDigestRatioOffsetAdjuster(1.16),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 57260.4,
    )
}
