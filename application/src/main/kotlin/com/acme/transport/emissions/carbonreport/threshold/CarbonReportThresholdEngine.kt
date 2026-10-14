package com.acme.transport.emissions.carbonreport.threshold

import com.acme.transport.kit.numbers.roundTo

/** One step of the carbon report threshold calculation. */
interface CarbonReportThresholdModifier {
    fun describe(): String
    fun adjust(item: CarbonReportThreshold, value: Double): Double
}

data class MinimumCarbonReportThresholdAdjuster(var minimum: Double) : CarbonReportThresholdModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CarbonReportThreshold, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingCarbonReportThresholdAdjuster(var step: Double) : CarbonReportThresholdModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CarbonReportThreshold, value: Double): Double = roundTo(value, step)
}

data class PercentCarbonReportThresholdAdjuster(var percent: Double) : CarbonReportThresholdModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CarbonReportThreshold, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the carbon report threshold steps in order and keeps a trace of each. */
data class CarbonReportThresholdEngine(
    var steps: MutableList<CarbonReportThresholdModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CarbonReportThreshold): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CarbonReportThresholdModifier = steps[index]
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

    fun evaluateAll(items: MutableList<CarbonReportThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCarbonReportThresholdEngine(): CarbonReportThresholdEngine {
    return CarbonReportThresholdEngine(
        steps = mutableListOf<CarbonReportThresholdModifier>(
            MinimumCarbonReportThresholdAdjuster(8.1),
            RoundingCarbonReportThresholdAdjuster(0.5),
            PercentCarbonReportThresholdAdjuster(2.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 93408.0,
    )
}
