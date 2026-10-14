package com.acme.transport.customs.clearance.reservation

import com.acme.transport.kit.numbers.roundTo

/** One step of the clearance summary calculation. */
interface ClearanceSummaryAdjuster {
    fun describe(): String
    fun adjust(item: ClearanceSummary, value: Double): Double
}

data class ClearanceSummaryBatchSizeOffsetAdjuster(var weight: Double) : ClearanceSummaryAdjuster {
    override fun describe(): String = "plus batch size times $weight"

    override fun adjust(item: ClearanceSummary, value: Double): Double = value + item.batchSize.toDouble() * weight
}

data class TieredClearanceSummaryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ClearanceSummaryAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ClearanceSummary, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingClearanceSummaryAdjuster(var step: Double) : ClearanceSummaryAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ClearanceSummary, value: Double): Double = roundTo(value, step)
}

/** Runs the clearance summary steps in order and keeps a trace of each. */
data class ClearanceSummaryEngine(
    var steps: MutableList<ClearanceSummaryAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ClearanceSummary): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ClearanceSummaryAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ClearanceSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultClearanceSummaryEngine(): ClearanceSummaryEngine {
    return ClearanceSummaryEngine(
        steps = mutableListOf<ClearanceSummaryAdjuster>(
            ClearanceSummaryBatchSizeOffsetAdjuster(1.51),
            TieredClearanceSummaryAdjuster(
                mutableListOf<Double>(2950.0, 6887.0, 9099.0),
                mutableListOf<Double>(1.1, 0.8, 1.12),
            ),
            RoundingClearanceSummaryAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 50615.4,
    )
}
