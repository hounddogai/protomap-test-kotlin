package com.acme.transport.emissions.carbonreport.tier

import com.acme.transport.kit.flags.Registry

/** One step of the weighted carbon report tier calculation. */
interface WeightedCarbonReportTierStep {
    fun describe(): String
    fun adjust(item: WeightedCarbonReportTier, value: Double): Double
}

data class PercentWeightedCarbonReportTierAdjuster(var percent: Double) : WeightedCarbonReportTierStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedCarbonReportTier, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumWeightedCarbonReportTierAdjuster(var minimum: Double) : WeightedCarbonReportTierStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedCarbonReportTier, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedWeightedCarbonReportTierAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedCarbonReportTierStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedCarbonReportTier, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the weighted carbon report tier steps in order and keeps a trace of each. */
data class WeightedCarbonReportTierEngine(
    var steps: MutableList<WeightedCarbonReportTierStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: WeightedCarbonReportTier): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedCarbonReportTierStep = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedCarbonReportTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultWeightedCarbonReportTierEngine(flags: Registry): WeightedCarbonReportTierEngine {
    return WeightedCarbonReportTierEngine(
        steps = mutableListOf<WeightedCarbonReportTierStep>(
            PercentWeightedCarbonReportTierAdjuster(9.8),
            MinimumWeightedCarbonReportTierAdjuster(18.3),
            FlaggedWeightedCarbonReportTierAdjuster(flags, "weighted-carbon-report-tier-tiered", -9.1),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 88908.5,
    )
}
