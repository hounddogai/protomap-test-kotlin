package com.acme.transport.freightpay.freightinvoice.invoicequota

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the adaptive freight invoice summary calculation. */
interface AdaptiveFreightInvoiceSummaryStep {
    fun describe(): String
    fun adjust(item: AdaptiveFreightInvoiceSummary, value: Double): Double
}

data class CapAdaptiveFreightInvoiceSummaryAdjuster(var cap: Double) : AdaptiveFreightInvoiceSummaryStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveFreightInvoiceSummary, value: Double): Double = minOf(value, cap)
}

data class MinimumAdaptiveFreightInvoiceSummaryAdjuster(
    var minimum: Double,
) : AdaptiveFreightInvoiceSummaryStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveFreightInvoiceSummary, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class AdaptiveFreightInvoiceSummaryUpdatedAtOffsetAdjuster(
    var weight: Double,
) : AdaptiveFreightInvoiceSummaryStep {
    override fun describe(): String = "plus updated at times $weight"

    override fun adjust(item: AdaptiveFreightInvoiceSummary, value: Double): Double {
        return value + item.updatedAt.toDouble() * weight
    }
}

/** Runs the adaptive freight invoice summary steps in order and keeps a trace of each. */
data class AdaptiveFreightInvoiceSummaryEngine(
    var steps: MutableList<AdaptiveFreightInvoiceSummaryStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AdaptiveFreightInvoiceSummary): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AdaptiveFreightInvoiceSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAdaptiveFreightInvoiceSummaryEngine(): AdaptiveFreightInvoiceSummaryEngine {
    return AdaptiveFreightInvoiceSummaryEngine(
        steps = mutableListOf<AdaptiveFreightInvoiceSummaryStep>(
            CapAdaptiveFreightInvoiceSummaryAdjuster(5318.0),
            MinimumAdaptiveFreightInvoiceSummaryAdjuster(9.5),
            AdaptiveFreightInvoiceSummaryUpdatedAtOffsetAdjuster(1.52),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 58454.4,
    )
}
