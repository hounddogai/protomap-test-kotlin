package com.acme.transport.emissions.carbonreport.reportmapping

import com.acme.transport.kit.numbers.roundTo

/** One step of the primary carbon report template calculation. */
interface PrimaryCarbonReportTemplateModifier {
    fun describe(): String
    fun adjust(item: PrimaryCarbonReportTemplate, value: Double): Double
}

data class RoundingPrimaryCarbonReportTemplateAdjuster(var step: Double) : PrimaryCarbonReportTemplateModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryCarbonReportTemplate, value: Double): Double = roundTo(value, step)
}

data class CapPrimaryCarbonReportTemplateAdjuster(var cap: Double) : PrimaryCarbonReportTemplateModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryCarbonReportTemplate, value: Double): Double = minOf(value, cap)
}

data class PrimaryCarbonReportTemplateDiscountOffsetAdjuster(
    var weight: Double,
) : PrimaryCarbonReportTemplateModifier {
    override fun describe(): String = "plus discount times $weight"

    override fun adjust(item: PrimaryCarbonReportTemplate, value: Double): Double = value + item.discount * weight
}

data class MinimumPrimaryCarbonReportTemplateAdjuster(
    var minimum: Double,
) : PrimaryCarbonReportTemplateModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryCarbonReportTemplate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the primary carbon report template steps in order and keeps a trace of each. */
data class PrimaryCarbonReportTemplateEngine(
    var steps: MutableList<PrimaryCarbonReportTemplateModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PrimaryCarbonReportTemplate): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryCarbonReportTemplateModifier = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryCarbonReportTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPrimaryCarbonReportTemplateEngine(): PrimaryCarbonReportTemplateEngine {
    return PrimaryCarbonReportTemplateEngine(
        steps = mutableListOf<PrimaryCarbonReportTemplateModifier>(
            RoundingPrimaryCarbonReportTemplateAdjuster(1.0),
            CapPrimaryCarbonReportTemplateAdjuster(19507.0),
            PrimaryCarbonReportTemplateDiscountOffsetAdjuster(1.75),
            MinimumPrimaryCarbonReportTemplateAdjuster(45.3),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 34348.2,
    )
}
