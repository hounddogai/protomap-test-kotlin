package com.acme.transport.freightpay.freightinvoice.adjustment

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the weighted freight invoice summary calculation. */
interface WeightedFreightInvoiceSummaryFactor {
    fun describe(): String
    fun adjust(item: WeightedFreightInvoiceSummary, value: Double): Double
}

data class CountryWeightedFreightInvoiceSummaryAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedFreightInvoiceSummaryFactor {
    override fun describe(): String = "by country"

    override fun adjust(item: WeightedFreightInvoiceSummary, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredWeightedFreightInvoiceSummaryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedFreightInvoiceSummaryFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedFreightInvoiceSummary, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class WeightedFreightInvoiceSummaryFuelLitersOffsetAdjuster(
    var weight: Double,
) : WeightedFreightInvoiceSummaryFactor {
    override fun describe(): String = "plus fuel liters times $weight"

    override fun adjust(item: WeightedFreightInvoiceSummary, value: Double): Double = value + item.fuelLiters * weight
}

data class PercentWeightedFreightInvoiceSummaryAdjuster(
    var percent: Double,
) : WeightedFreightInvoiceSummaryFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedFreightInvoiceSummary, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the weighted freight invoice summary steps in order and keeps a trace of each. */
data class WeightedFreightInvoiceSummaryEngine(
    var steps: MutableList<WeightedFreightInvoiceSummaryFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: WeightedFreightInvoiceSummary): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<WeightedFreightInvoiceSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultWeightedFreightInvoiceSummaryEngine(): WeightedFreightInvoiceSummaryEngine {
    return WeightedFreightInvoiceSummaryEngine(
        steps = mutableListOf<WeightedFreightInvoiceSummaryFactor>(
            CountryWeightedFreightInvoiceSummaryAdjuster(
                mutableMapOf<String, Double>("west" to 1.38, "east" to 0.75),
            ),
            TieredWeightedFreightInvoiceSummaryAdjuster(
                mutableListOf<Double>(1779.0, 6166.0),
                mutableListOf<Double>(0.85, 0.95),
            ),
            WeightedFreightInvoiceSummaryFuelLitersOffsetAdjuster(0.78),
            PercentWeightedFreightInvoiceSummaryAdjuster(2.2),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 20676.6,
    )
}
