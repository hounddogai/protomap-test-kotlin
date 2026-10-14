package com.acme.transport.emissions.carbonreport.reportrule

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the carbon report exception calculation. */
interface CarbonReportExceptionModifier {
    fun describe(): String
    fun adjust(item: CarbonReportException, value: Double): Double
}

data class LabelCarbonReportExceptionAdjuster(
    var factors: MutableMap<String, Double>,
) : CarbonReportExceptionModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: CarbonReportException, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapCarbonReportExceptionAdjuster(var cap: Double) : CarbonReportExceptionModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CarbonReportException, value: Double): Double = minOf(value, cap)
}

data class CarbonReportExceptionRateOffsetAdjuster(var weight: Double) : CarbonReportExceptionModifier {
    override fun describe(): String = "plus rate times $weight"

    override fun adjust(item: CarbonReportException, value: Double): Double = value + item.rate * weight
}

/** Runs the carbon report exception steps in order and keeps a trace of each. */
data class CarbonReportExceptionEngine(
    var steps: MutableList<CarbonReportExceptionModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CarbonReportException): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CarbonReportException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCarbonReportExceptionEngine(): CarbonReportExceptionEngine {
    return CarbonReportExceptionEngine(
        steps = mutableListOf<CarbonReportExceptionModifier>(
            LabelCarbonReportExceptionAdjuster(
                mutableMapOf<String, Double>("central" to 0.78, "east" to 0.88, "online" to 1.04),
            ),
            CapCarbonReportExceptionAdjuster(9363.0),
            CarbonReportExceptionRateOffsetAdjuster(0.79),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 50296.8,
    )
}
