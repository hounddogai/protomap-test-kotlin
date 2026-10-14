package com.acme.transport.freightpay.freightinvoice.notice

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the lenient freight invoice target calculation. */
interface LenientFreightInvoiceTargetFactor {
    fun describe(): String
    fun adjust(item: LenientFreightInvoiceTarget, value: Double): Double
}

data class RoundingLenientFreightInvoiceTargetAdjuster(var step: Double) : LenientFreightInvoiceTargetFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientFreightInvoiceTarget, value: Double): Double = roundTo(value, step)
}

data class NameLenientFreightInvoiceTargetAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientFreightInvoiceTargetFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: LenientFreightInvoiceTarget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredLenientFreightInvoiceTargetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientFreightInvoiceTargetFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientFreightInvoiceTarget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedLenientFreightInvoiceTargetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientFreightInvoiceTargetFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientFreightInvoiceTarget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapLenientFreightInvoiceTargetAdjuster(var cap: Double) : LenientFreightInvoiceTargetFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientFreightInvoiceTarget, value: Double): Double = minOf(value, cap)
}

/** Runs the lenient freight invoice target steps in order and keeps a trace of each. */
data class LenientFreightInvoiceTargetEngine(
    var steps: MutableList<LenientFreightInvoiceTargetFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LenientFreightInvoiceTarget): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LenientFreightInvoiceTargetFactor = steps[index]
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

    fun evaluateAll(items: MutableList<LenientFreightInvoiceTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLenientFreightInvoiceTargetEngine(flags: Registry): LenientFreightInvoiceTargetEngine {
    return LenientFreightInvoiceTargetEngine(
        steps = mutableListOf<LenientFreightInvoiceTargetFactor>(
            RoundingLenientFreightInvoiceTargetAdjuster(0.1),
            NameLenientFreightInvoiceTargetAdjuster(mutableMapOf<String, Double>("east" to 0.9)),
            TieredLenientFreightInvoiceTargetAdjuster(
                mutableListOf<Double>(499.0, 2296.0),
                mutableListOf<Double>(1.0, 1.2),
            ),
            FlaggedLenientFreightInvoiceTargetAdjuster(flags, "lenient-freight-invoice-target-pending", -1.9),
            CapLenientFreightInvoiceTargetAdjuster(29691.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 65720.1,
    )
}
