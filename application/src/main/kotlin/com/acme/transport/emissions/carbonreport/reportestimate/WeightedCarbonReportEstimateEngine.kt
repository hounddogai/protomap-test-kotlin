package com.acme.transport.emissions.carbonreport.reportestimate

/** One step of the weighted carbon report estimate calculation. */
interface WeightedCarbonReportEstimateFactor {
    fun describe(): String
    fun adjust(item: WeightedCarbonReportEstimate, value: Double): Double
}

data class PercentWeightedCarbonReportEstimateAdjuster(
    var percent: Double,
) : WeightedCarbonReportEstimateFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedCarbonReportEstimate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapWeightedCarbonReportEstimateAdjuster(var cap: Double) : WeightedCarbonReportEstimateFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedCarbonReportEstimate, value: Double): Double = minOf(value, cap)
}

data class DepotWeightedCarbonReportEstimateAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedCarbonReportEstimateFactor {
    override fun describe(): String = "by depot"

    override fun adjust(item: WeightedCarbonReportEstimate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the weighted carbon report estimate steps in order and keeps a trace of each. */
data class WeightedCarbonReportEstimateEngine(
    var steps: MutableList<WeightedCarbonReportEstimateFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: WeightedCarbonReportEstimate): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedCarbonReportEstimateFactor = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedCarbonReportEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultWeightedCarbonReportEstimateEngine(): WeightedCarbonReportEstimateEngine {
    return WeightedCarbonReportEstimateEngine(
        steps = mutableListOf<WeightedCarbonReportEstimateFactor>(
            PercentWeightedCarbonReportEstimateAdjuster(-18.0),
            CapWeightedCarbonReportEstimateAdjuster(10198.0),
            DepotWeightedCarbonReportEstimateAdjuster(
                mutableMapOf<String, Double>("east" to 0.74, "online" to 0.73),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 34119.1,
    )
}
