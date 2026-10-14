package com.acme.transport.emissions.carbonreport.estimate

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the carbon report estimate calculation. */
interface CarbonReportEstimateStep {
    fun describe(): String
    fun adjust(item: CarbonReportEstimate, value: Double): Double
}

data class CapCarbonReportEstimateAdjuster(var cap: Double) : CarbonReportEstimateStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CarbonReportEstimate, value: Double): Double = minOf(value, cap)
}

data class TieredCarbonReportEstimateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CarbonReportEstimateStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CarbonReportEstimate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingCarbonReportEstimateAdjuster(var step: Double) : CarbonReportEstimateStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CarbonReportEstimate, value: Double): Double = roundTo(value, step)
}

data class MinimumCarbonReportEstimateAdjuster(var minimum: Double) : CarbonReportEstimateStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CarbonReportEstimate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentCarbonReportEstimateAdjuster(var percent: Double) : CarbonReportEstimateStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CarbonReportEstimate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedCarbonReportEstimateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CarbonReportEstimateStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CarbonReportEstimate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the carbon report estimate steps in order and keeps a trace of each. */
data class CarbonReportEstimateEngine(
    var steps: MutableList<CarbonReportEstimateStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CarbonReportEstimate): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CarbonReportEstimateStep = steps[index]
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

    fun evaluateAll(items: MutableList<CarbonReportEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCarbonReportEstimateEngine(flags: Registry): CarbonReportEstimateEngine {
    return CarbonReportEstimateEngine(
        steps = mutableListOf<CarbonReportEstimateStep>(
            CapCarbonReportEstimateAdjuster(28058.0),
            TieredCarbonReportEstimateAdjuster(
                mutableListOf<Double>(2860.0, 5594.0, 7147.0, 8105.0),
                mutableListOf<Double>(0.83, 1.02, 0.92, 0.89),
            ),
            RoundingCarbonReportEstimateAdjuster(0.5),
            MinimumCarbonReportEstimateAdjuster(45.5),
            PercentCarbonReportEstimateAdjuster(-16.1),
            FlaggedCarbonReportEstimateAdjuster(flags, "carbon-report-estimate-legacy", 8.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 15183.0,
    )
}
