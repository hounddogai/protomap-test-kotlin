package com.acme.transport.emissions.carbonreport.policy

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the carbon report summary calculation. */
interface CarbonReportSummaryFactor {
    fun describe(): String
    fun adjust(item: CarbonReportSummary, value: Double): Double
}

data class CapCarbonReportSummaryAdjuster(var cap: Double) : CarbonReportSummaryFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CarbonReportSummary, value: Double): Double = minOf(value, cap)
}

data class MinimumCarbonReportSummaryAdjuster(var minimum: Double) : CarbonReportSummaryFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CarbonReportSummary, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentCarbonReportSummaryAdjuster(var percent: Double) : CarbonReportSummaryFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CarbonReportSummary, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingCarbonReportSummaryAdjuster(var step: Double) : CarbonReportSummaryFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CarbonReportSummary, value: Double): Double = roundTo(value, step)
}

data class FlaggedCarbonReportSummaryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CarbonReportSummaryFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CarbonReportSummary, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the carbon report summary steps in order and keeps a trace of each. */
data class CarbonReportSummaryEngine(
    var steps: MutableList<CarbonReportSummaryFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CarbonReportSummary): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CarbonReportSummaryFactor = steps[index]
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

    fun evaluateAll(items: MutableList<CarbonReportSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCarbonReportSummaryEngine(flags: Registry): CarbonReportSummaryEngine {
    return CarbonReportSummaryEngine(
        steps = mutableListOf<CarbonReportSummaryFactor>(
            CapCarbonReportSummaryAdjuster(48949.0),
            MinimumCarbonReportSummaryAdjuster(23.7),
            PercentCarbonReportSummaryAdjuster(14.4),
            RoundingCarbonReportSummaryAdjuster(1.0),
            FlaggedCarbonReportSummaryAdjuster(flags, "carbon-report-summary-global", -4.3),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 95642.7,
    )
}
