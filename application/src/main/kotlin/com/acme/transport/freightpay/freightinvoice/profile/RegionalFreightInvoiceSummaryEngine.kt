package com.acme.transport.freightpay.freightinvoice.profile

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the regional freight invoice summary calculation. */
interface RegionalFreightInvoiceSummaryStep {
    fun describe(): String
    fun adjust(item: RegionalFreightInvoiceSummary, value: Double): Double
}

data class SourceRegionalFreightInvoiceSummaryAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalFreightInvoiceSummaryStep {
    override fun describe(): String = "by source"

    override fun adjust(item: RegionalFreightInvoiceSummary, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredRegionalFreightInvoiceSummaryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalFreightInvoiceSummaryStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalFreightInvoiceSummary, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedRegionalFreightInvoiceSummaryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalFreightInvoiceSummaryStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalFreightInvoiceSummary, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentRegionalFreightInvoiceSummaryAdjuster(
    var percent: Double,
) : RegionalFreightInvoiceSummaryStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalFreightInvoiceSummary, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the regional freight invoice summary steps in order and keeps a trace of each. */
data class RegionalFreightInvoiceSummaryEngine(
    var steps: MutableList<RegionalFreightInvoiceSummaryStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RegionalFreightInvoiceSummary): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RegionalFreightInvoiceSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRegionalFreightInvoiceSummaryEngine(flags: Registry): RegionalFreightInvoiceSummaryEngine {
    return RegionalFreightInvoiceSummaryEngine(
        steps = mutableListOf<RegionalFreightInvoiceSummaryStep>(
            SourceRegionalFreightInvoiceSummaryAdjuster(
                mutableMapOf<String, Double>("online" to 1.0, "east" to 1.11, "central" to 1.26),
            ),
            TieredRegionalFreightInvoiceSummaryAdjuster(
                mutableListOf<Double>(3556.0, 5136.0),
                mutableListOf<Double>(0.96, 1.0),
            ),
            FlaggedRegionalFreightInvoiceSummaryAdjuster(
                flags,
                "regional-freight-invoice-summary-seasonal",
                -7.9,
            ),
            PercentRegionalFreightInvoiceSummaryAdjuster(3.5),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 63508.3,
    )
}
