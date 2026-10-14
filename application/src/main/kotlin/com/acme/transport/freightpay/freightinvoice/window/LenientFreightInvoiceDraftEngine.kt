package com.acme.transport.freightpay.freightinvoice.window

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the lenient freight invoice draft calculation. */
interface LenientFreightInvoiceDraftStep {
    fun describe(): String
    fun adjust(item: LenientFreightInvoiceDraft, value: Double): Double
}

data class RoundingLenientFreightInvoiceDraftAdjuster(var step: Double) : LenientFreightInvoiceDraftStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientFreightInvoiceDraft, value: Double): Double = roundTo(value, step)
}

data class PercentLenientFreightInvoiceDraftAdjuster(var percent: Double) : LenientFreightInvoiceDraftStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientFreightInvoiceDraft, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedLenientFreightInvoiceDraftAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientFreightInvoiceDraftStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientFreightInvoiceDraft, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumLenientFreightInvoiceDraftAdjuster(var minimum: Double) : LenientFreightInvoiceDraftStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientFreightInvoiceDraft, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredLenientFreightInvoiceDraftAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientFreightInvoiceDraftStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientFreightInvoiceDraft, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LabelLenientFreightInvoiceDraftAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientFreightInvoiceDraftStep {
    override fun describe(): String = "by label"

    override fun adjust(item: LenientFreightInvoiceDraft, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the lenient freight invoice draft steps in order and keeps a trace of each. */
data class LenientFreightInvoiceDraftEngine(
    var steps: MutableList<LenientFreightInvoiceDraftStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LenientFreightInvoiceDraft): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LenientFreightInvoiceDraftStep = steps[index]
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

    fun evaluateAll(items: MutableList<LenientFreightInvoiceDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLenientFreightInvoiceDraftEngine(flags: Registry): LenientFreightInvoiceDraftEngine {
    return LenientFreightInvoiceDraftEngine(
        steps = mutableListOf<LenientFreightInvoiceDraftStep>(
            RoundingLenientFreightInvoiceDraftAdjuster(0.1),
            PercentLenientFreightInvoiceDraftAdjuster(24.2),
            FlaggedLenientFreightInvoiceDraftAdjuster(flags, "lenient-freight-invoice-draft-weighted", 9.8),
            MinimumLenientFreightInvoiceDraftAdjuster(38.3),
            TieredLenientFreightInvoiceDraftAdjuster(
                mutableListOf<Double>(3190.0, 7748.0, 9855.0),
                mutableListOf<Double>(0.97, 1.05, 0.89),
            ),
            LabelLenientFreightInvoiceDraftAdjuster(mutableMapOf<String, Double>("online" to 0.89)),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 92361.1,
    )
}
