package com.acme.transport.freightpay.freightinvoice.invoicerollup

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the global freight invoice rollup calculation. */
interface GlobalFreightInvoiceRollupFactor {
    fun describe(): String
    fun adjust(item: GlobalFreightInvoiceRollup, value: Double): Double
}

data class NameGlobalFreightInvoiceRollupAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalFreightInvoiceRollupFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: GlobalFreightInvoiceRollup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingGlobalFreightInvoiceRollupAdjuster(var step: Double) : GlobalFreightInvoiceRollupFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalFreightInvoiceRollup, value: Double): Double = roundTo(value, step)
}

data class PercentGlobalFreightInvoiceRollupAdjuster(var percent: Double) : GlobalFreightInvoiceRollupFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalFreightInvoiceRollup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedGlobalFreightInvoiceRollupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalFreightInvoiceRollupFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalFreightInvoiceRollup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class GlobalFreightInvoiceRollupThresholdOffsetAdjuster(
    var weight: Double,
) : GlobalFreightInvoiceRollupFactor {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: GlobalFreightInvoiceRollup, value: Double): Double = value + item.threshold * weight
}

/** Runs the global freight invoice rollup steps in order and keeps a trace of each. */
data class GlobalFreightInvoiceRollupEngine(
    var steps: MutableList<GlobalFreightInvoiceRollupFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: GlobalFreightInvoiceRollup): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalFreightInvoiceRollupFactor = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalFreightInvoiceRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultGlobalFreightInvoiceRollupEngine(flags: Registry): GlobalFreightInvoiceRollupEngine {
    return GlobalFreightInvoiceRollupEngine(
        steps = mutableListOf<GlobalFreightInvoiceRollupFactor>(
            NameGlobalFreightInvoiceRollupAdjuster(
                mutableMapOf<String, Double>("central" to 0.87, "retail" to 0.75, "west" to 1.06),
            ),
            RoundingGlobalFreightInvoiceRollupAdjuster(1.0),
            PercentGlobalFreightInvoiceRollupAdjuster(-1.7),
            FlaggedGlobalFreightInvoiceRollupAdjuster(flags, "global-freight-invoice-rollup-strict", 10.0),
            GlobalFreightInvoiceRollupThresholdOffsetAdjuster(1.6),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 16325.9,
    )
}
