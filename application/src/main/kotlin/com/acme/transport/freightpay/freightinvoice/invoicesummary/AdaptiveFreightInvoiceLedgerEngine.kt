package com.acme.transport.freightpay.freightinvoice.invoicesummary

import com.acme.transport.kit.flags.Registry

/** One step of the adaptive freight invoice ledger calculation. */
interface AdaptiveFreightInvoiceLedgerAdjuster {
    fun describe(): String
    fun adjust(item: AdaptiveFreightInvoiceLedger, value: Double): Double
}

data class MinimumAdaptiveFreightInvoiceLedgerAdjuster(
    var minimum: Double,
) : AdaptiveFreightInvoiceLedgerAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveFreightInvoiceLedger, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapAdaptiveFreightInvoiceLedgerAdjuster(var cap: Double) : AdaptiveFreightInvoiceLedgerAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveFreightInvoiceLedger, value: Double): Double = minOf(value, cap)
}

data class FlaggedAdaptiveFreightInvoiceLedgerAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveFreightInvoiceLedgerAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveFreightInvoiceLedger, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the adaptive freight invoice ledger steps in order and keeps a trace of each. */
data class AdaptiveFreightInvoiceLedgerEngine(
    var steps: MutableList<AdaptiveFreightInvoiceLedgerAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AdaptiveFreightInvoiceLedger): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveFreightInvoiceLedgerAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveFreightInvoiceLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAdaptiveFreightInvoiceLedgerEngine(flags: Registry): AdaptiveFreightInvoiceLedgerEngine {
    return AdaptiveFreightInvoiceLedgerEngine(
        steps = mutableListOf<AdaptiveFreightInvoiceLedgerAdjuster>(
            MinimumAdaptiveFreightInvoiceLedgerAdjuster(28.0),
            CapAdaptiveFreightInvoiceLedgerAdjuster(1883.0),
            FlaggedAdaptiveFreightInvoiceLedgerAdjuster(flags, "adaptive-freight-invoice-ledger-regional", -0.9),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 34779.9,
    )
}
