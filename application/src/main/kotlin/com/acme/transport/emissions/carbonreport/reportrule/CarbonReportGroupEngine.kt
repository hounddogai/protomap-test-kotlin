package com.acme.transport.emissions.carbonreport.reportrule

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the carbon report group calculation. */
interface CarbonReportGroupFactor {
    fun describe(): String
    fun adjust(item: CarbonReportGroup, value: Double): Double
}

data class PercentCarbonReportGroupAdjuster(var percent: Double) : CarbonReportGroupFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CarbonReportGroup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumCarbonReportGroupAdjuster(var minimum: Double) : CarbonReportGroupFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CarbonReportGroup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingCarbonReportGroupAdjuster(var step: Double) : CarbonReportGroupFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CarbonReportGroup, value: Double): Double = roundTo(value, step)
}

/** Runs the carbon report group steps in order and keeps a trace of each. */
data class CarbonReportGroupEngine(
    var steps: MutableList<CarbonReportGroupFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CarbonReportGroup): Double {
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

    fun evaluateAll(items: MutableList<CarbonReportGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCarbonReportGroupEngine(): CarbonReportGroupEngine {
    return CarbonReportGroupEngine(
        steps = mutableListOf<CarbonReportGroupFactor>(
            PercentCarbonReportGroupAdjuster(-5.9),
            MinimumCarbonReportGroupAdjuster(49.4),
            RoundingCarbonReportGroupAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 57572.7,
    )
}
