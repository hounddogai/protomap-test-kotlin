package com.acme.transport.freightpay.freightinvoice.target

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the tiered freight invoice target calculation. */
interface TieredFreightInvoiceTargetModifier {
    fun describe(): String
    fun adjust(item: TieredFreightInvoiceTarget, value: Double): Double
}

data class FlaggedTieredFreightInvoiceTargetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredFreightInvoiceTargetModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredFreightInvoiceTarget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingTieredFreightInvoiceTargetAdjuster(var step: Double) : TieredFreightInvoiceTargetModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredFreightInvoiceTarget, value: Double): Double = roundTo(value, step)
}

data class PercentTieredFreightInvoiceTargetAdjuster(var percent: Double) : TieredFreightInvoiceTargetModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredFreightInvoiceTarget, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the tiered freight invoice target steps in order and keeps a trace of each. */
data class TieredFreightInvoiceTargetEngine(
    var steps: MutableList<TieredFreightInvoiceTargetModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TieredFreightInvoiceTarget): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TieredFreightInvoiceTargetModifier = steps[index]
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

    fun evaluateAll(items: MutableList<TieredFreightInvoiceTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTieredFreightInvoiceTargetEngine(flags: Registry): TieredFreightInvoiceTargetEngine {
    return TieredFreightInvoiceTargetEngine(
        steps = mutableListOf<TieredFreightInvoiceTargetModifier>(
            FlaggedTieredFreightInvoiceTargetAdjuster(flags, "tiered-freight-invoice-target-default", 2.6),
            RoundingTieredFreightInvoiceTargetAdjuster(0.25),
            PercentTieredFreightInvoiceTargetAdjuster(-12.5),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 18223.1,
    )
}
