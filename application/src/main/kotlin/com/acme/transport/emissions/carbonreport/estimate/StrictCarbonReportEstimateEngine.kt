package com.acme.transport.emissions.carbonreport.estimate

import com.acme.transport.kit.numbers.roundTo

/** One step of the strict carbon report estimate calculation. */
interface StrictCarbonReportEstimateFactor {
    fun describe(): String
    fun adjust(item: StrictCarbonReportEstimate, value: Double): Double
}

data class RoundingStrictCarbonReportEstimateAdjuster(var step: Double) : StrictCarbonReportEstimateFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictCarbonReportEstimate, value: Double): Double = roundTo(value, step)
}

data class TieredStrictCarbonReportEstimateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictCarbonReportEstimateFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictCarbonReportEstimate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumStrictCarbonReportEstimateAdjuster(var minimum: Double) : StrictCarbonReportEstimateFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictCarbonReportEstimate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class SourceStrictCarbonReportEstimateAdjuster(
    var factors: MutableMap<String, Double>,
) : StrictCarbonReportEstimateFactor {
    override fun describe(): String = "by source"

    override fun adjust(item: StrictCarbonReportEstimate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the strict carbon report estimate steps in order and keeps a trace of each. */
data class StrictCarbonReportEstimateEngine(
    var steps: MutableList<StrictCarbonReportEstimateFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: StrictCarbonReportEstimate): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictCarbonReportEstimateFactor = steps[index]
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

    fun evaluateAll(items: MutableList<StrictCarbonReportEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultStrictCarbonReportEstimateEngine(): StrictCarbonReportEstimateEngine {
    return StrictCarbonReportEstimateEngine(
        steps = mutableListOf<StrictCarbonReportEstimateFactor>(
            RoundingStrictCarbonReportEstimateAdjuster(0.5),
            TieredStrictCarbonReportEstimateAdjuster(
                mutableListOf<Double>(766.0, 3179.0, 4794.0),
                mutableListOf<Double>(1.08, 0.99, 1.0),
            ),
            MinimumStrictCarbonReportEstimateAdjuster(39.6),
            SourceStrictCarbonReportEstimateAdjuster(
                mutableMapOf<String, Double>("north" to 1.0, "online" to 1.23),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 31314.5,
    )
}
