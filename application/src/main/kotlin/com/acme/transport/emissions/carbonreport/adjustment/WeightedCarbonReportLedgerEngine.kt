package com.acme.transport.emissions.carbonreport.adjustment

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the weighted carbon report ledger calculation. */
interface WeightedCarbonReportLedgerFactor {
    fun describe(): String
    fun adjust(item: WeightedCarbonReportLedger, value: Double): Double
}

data class MinimumWeightedCarbonReportLedgerAdjuster(var minimum: Double) : WeightedCarbonReportLedgerFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedCarbonReportLedger, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class NameWeightedCarbonReportLedgerAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedCarbonReportLedgerFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: WeightedCarbonReportLedger, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapWeightedCarbonReportLedgerAdjuster(var cap: Double) : WeightedCarbonReportLedgerFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedCarbonReportLedger, value: Double): Double = minOf(value, cap)
}

/** Runs the weighted carbon report ledger steps in order and keeps a trace of each. */
data class WeightedCarbonReportLedgerEngine(
    var steps: MutableList<WeightedCarbonReportLedgerFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: WeightedCarbonReportLedger): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<WeightedCarbonReportLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultWeightedCarbonReportLedgerEngine(): WeightedCarbonReportLedgerEngine {
    return WeightedCarbonReportLedgerEngine(
        steps = mutableListOf<WeightedCarbonReportLedgerFactor>(
            MinimumWeightedCarbonReportLedgerAdjuster(15.1),
            NameWeightedCarbonReportLedgerAdjuster(mutableMapOf<String, Double>("west" to 0.73)),
            CapWeightedCarbonReportLedgerAdjuster(20487.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 72105.6,
    )
}
