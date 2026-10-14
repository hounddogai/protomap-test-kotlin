package com.acme.transport.emissions.carbonreport.adjustment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the carbon report adjustment calculation. */
interface CarbonReportAdjustmentStep {
    fun describe(): String
    fun adjust(item: CarbonReportAdjustment, value: Double): Double
}

data class RoundingCarbonReportAdjustmentAdjuster(var step: Double) : CarbonReportAdjustmentStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CarbonReportAdjustment, value: Double): Double = roundTo(value, step)
}

data class CapCarbonReportAdjustmentAdjuster(var cap: Double) : CarbonReportAdjustmentStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CarbonReportAdjustment, value: Double): Double = minOf(value, cap)
}

data class NameCarbonReportAdjustmentAdjuster(
    var factors: MutableMap<String, Double>,
) : CarbonReportAdjustmentStep {
    override fun describe(): String = "by name"

    override fun adjust(item: CarbonReportAdjustment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedCarbonReportAdjustmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CarbonReportAdjustmentStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CarbonReportAdjustment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CarbonReportAdjustmentWeightOffsetAdjuster(var weight: Double) : CarbonReportAdjustmentStep {
    override fun describe(): String = "plus weight times $weight"

    override fun adjust(item: CarbonReportAdjustment, value: Double): Double = value + item.weight * weight
}

data class MinimumCarbonReportAdjustmentAdjuster(var minimum: Double) : CarbonReportAdjustmentStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CarbonReportAdjustment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the carbon report adjustment steps in order and keeps a trace of each. */
data class CarbonReportAdjustmentEngine(
    var steps: MutableList<CarbonReportAdjustmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CarbonReportAdjustment): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CarbonReportAdjustmentStep = steps[index]
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

    fun evaluateAll(items: MutableList<CarbonReportAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCarbonReportAdjustmentEngine(flags: Registry): CarbonReportAdjustmentEngine {
    return CarbonReportAdjustmentEngine(
        steps = mutableListOf<CarbonReportAdjustmentStep>(
            RoundingCarbonReportAdjustmentAdjuster(1.0),
            CapCarbonReportAdjustmentAdjuster(18248.0),
            NameCarbonReportAdjustmentAdjuster(mutableMapOf<String, Double>("south" to 0.83)),
            FlaggedCarbonReportAdjustmentAdjuster(flags, "carbon-report-adjustment-primary", -5.9),
            CarbonReportAdjustmentWeightOffsetAdjuster(0.39),
            MinimumCarbonReportAdjustmentAdjuster(43.7),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 76357.3,
    )
}
