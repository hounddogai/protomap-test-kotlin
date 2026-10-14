package com.acme.transport.emissions.carbonreport.quota

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the carbon report mapping calculation. */
interface CarbonReportMappingStep {
    fun describe(): String
    fun adjust(item: CarbonReportMapping, value: Double): Double
}

data class RoundingCarbonReportMappingAdjuster(var step: Double) : CarbonReportMappingStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CarbonReportMapping, value: Double): Double = roundTo(value, step)
}

data class FlaggedCarbonReportMappingAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CarbonReportMappingStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CarbonReportMapping, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumCarbonReportMappingAdjuster(var minimum: Double) : CarbonReportMappingStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CarbonReportMapping, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapCarbonReportMappingAdjuster(var cap: Double) : CarbonReportMappingStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CarbonReportMapping, value: Double): Double = minOf(value, cap)
}

data class PercentCarbonReportMappingAdjuster(var percent: Double) : CarbonReportMappingStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CarbonReportMapping, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the carbon report mapping steps in order and keeps a trace of each. */
data class CarbonReportMappingEngine(
    var steps: MutableList<CarbonReportMappingStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CarbonReportMapping): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CarbonReportMappingStep = steps[index]
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

    fun evaluateAll(items: MutableList<CarbonReportMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCarbonReportMappingEngine(flags: Registry): CarbonReportMappingEngine {
    return CarbonReportMappingEngine(
        steps = mutableListOf<CarbonReportMappingStep>(
            RoundingCarbonReportMappingAdjuster(0.25),
            FlaggedCarbonReportMappingAdjuster(flags, "carbon-report-mapping-batched", -4.3),
            MinimumCarbonReportMappingAdjuster(27.2),
            CapCarbonReportMappingAdjuster(33582.0),
            PercentCarbonReportMappingAdjuster(-10.1),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 82845.1,
    )
}
