package com.acme.transport.emissions.carbonreport.template

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the carbon report template calculation. */
interface CarbonReportTemplateStep {
    fun describe(): String
    fun adjust(item: CarbonReportTemplate, value: Double): Double
}

data class MinimumCarbonReportTemplateAdjuster(var minimum: Double) : CarbonReportTemplateStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CarbonReportTemplate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentCarbonReportTemplateAdjuster(var percent: Double) : CarbonReportTemplateStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CarbonReportTemplate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapCarbonReportTemplateAdjuster(var cap: Double) : CarbonReportTemplateStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CarbonReportTemplate, value: Double): Double = minOf(value, cap)
}

data class CarbonReportTemplateExpiresAtOffsetAdjuster(var weight: Double) : CarbonReportTemplateStep {
    override fun describe(): String = "plus expires at times $weight"

    override fun adjust(item: CarbonReportTemplate, value: Double): Double {
        return value + item.expiresAt.toDouble() * weight
    }
}

/** Runs the carbon report template steps in order and keeps a trace of each. */
data class CarbonReportTemplateEngine(
    var steps: MutableList<CarbonReportTemplateStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CarbonReportTemplate): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CarbonReportTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCarbonReportTemplateEngine(): CarbonReportTemplateEngine {
    return CarbonReportTemplateEngine(
        steps = mutableListOf<CarbonReportTemplateStep>(
            MinimumCarbonReportTemplateAdjuster(29.0),
            PercentCarbonReportTemplateAdjuster(15.2),
            CapCarbonReportTemplateAdjuster(23588.0),
            CarbonReportTemplateExpiresAtOffsetAdjuster(1.04),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 71480.4,
    )
}
