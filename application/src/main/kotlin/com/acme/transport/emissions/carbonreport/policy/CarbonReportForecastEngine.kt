package com.acme.transport.emissions.carbonreport.policy

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the carbon report forecast calculation. */
interface CarbonReportForecastModifier {
    fun describe(): String
    fun adjust(item: CarbonReportForecast, value: Double): Double
}

data class RoundingCarbonReportForecastAdjuster(var step: Double) : CarbonReportForecastModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CarbonReportForecast, value: Double): Double = roundTo(value, step)
}

data class CarbonReportForecastScoreOffsetAdjuster(var weight: Double) : CarbonReportForecastModifier {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: CarbonReportForecast, value: Double): Double = value + item.score * weight
}

data class PercentCarbonReportForecastAdjuster(var percent: Double) : CarbonReportForecastModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CarbonReportForecast, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the carbon report forecast steps in order and keeps a trace of each. */
data class CarbonReportForecastEngine(
    var steps: MutableList<CarbonReportForecastModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CarbonReportForecast): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CarbonReportForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCarbonReportForecastEngine(): CarbonReportForecastEngine {
    return CarbonReportForecastEngine(
        steps = mutableListOf<CarbonReportForecastModifier>(
            RoundingCarbonReportForecastAdjuster(0.05),
            CarbonReportForecastScoreOffsetAdjuster(0.16),
            PercentCarbonReportForecastAdjuster(-8.7),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 93285.8,
    )
}
