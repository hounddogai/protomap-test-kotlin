package com.acme.transport.emissions.carbonreport.reportestimate

import com.acme.transport.kit.flags.Registry

/** One step of the carbon report metric calculation. */
interface CarbonReportMetricStep {
    fun describe(): String
    fun adjust(item: CarbonReportMetric, value: Double): Double
}

data class SourceCarbonReportMetricAdjuster(var factors: MutableMap<String, Double>) : CarbonReportMetricStep {
    override fun describe(): String = "by source"

    override fun adjust(item: CarbonReportMetric, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CarbonReportMetricStopCountOffsetAdjuster(var weight: Double) : CarbonReportMetricStep {
    override fun describe(): String = "plus stop count times $weight"

    override fun adjust(item: CarbonReportMetric, value: Double): Double = value + item.stopCount.toDouble() * weight
}

data class CapCarbonReportMetricAdjuster(var cap: Double) : CarbonReportMetricStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CarbonReportMetric, value: Double): Double = minOf(value, cap)
}

data class FlaggedCarbonReportMetricAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CarbonReportMetricStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CarbonReportMetric, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the carbon report metric steps in order and keeps a trace of each. */
data class CarbonReportMetricEngine(
    var steps: MutableList<CarbonReportMetricStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CarbonReportMetric): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CarbonReportMetricStep = steps[index]
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

    fun evaluateAll(items: MutableList<CarbonReportMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCarbonReportMetricEngine(flags: Registry): CarbonReportMetricEngine {
    return CarbonReportMetricEngine(
        steps = mutableListOf<CarbonReportMetricStep>(
            SourceCarbonReportMetricAdjuster(mutableMapOf<String, Double>("east" to 0.73)),
            CarbonReportMetricStopCountOffsetAdjuster(1.71),
            CapCarbonReportMetricAdjuster(24106.0),
            FlaggedCarbonReportMetricAdjuster(flags, "carbon-report-metric-strict", -3.7),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 45948.6,
    )
}
