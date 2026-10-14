package com.acme.transport.emissions.carbonreport.recorditem

import com.acme.transport.kit.numbers.roundTo

/** One step of the pending carbon report milestone calculation. */
interface PendingCarbonReportMilestoneStep {
    fun describe(): String
    fun adjust(item: PendingCarbonReportMilestone, value: Double): Double
}

data class PercentPendingCarbonReportMilestoneAdjuster(var percent: Double) : PendingCarbonReportMilestoneStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingCarbonReportMilestone, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredPendingCarbonReportMilestoneAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingCarbonReportMilestoneStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingCarbonReportMilestone, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingPendingCarbonReportMilestoneAdjuster(var step: Double) : PendingCarbonReportMilestoneStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingCarbonReportMilestone, value: Double): Double = roundTo(value, step)
}

data class MinimumPendingCarbonReportMilestoneAdjuster(var minimum: Double) : PendingCarbonReportMilestoneStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingCarbonReportMilestone, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the pending carbon report milestone steps in order and keeps a trace of each. */
data class PendingCarbonReportMilestoneEngine(
    var steps: MutableList<PendingCarbonReportMilestoneStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PendingCarbonReportMilestone): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingCarbonReportMilestoneStep = steps[index]
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

    fun evaluateAll(items: MutableList<PendingCarbonReportMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPendingCarbonReportMilestoneEngine(): PendingCarbonReportMilestoneEngine {
    return PendingCarbonReportMilestoneEngine(
        steps = mutableListOf<PendingCarbonReportMilestoneStep>(
            PercentPendingCarbonReportMilestoneAdjuster(21.2),
            TieredPendingCarbonReportMilestoneAdjuster(
                mutableListOf<Double>(1447.0, 1595.0),
                mutableListOf<Double>(1.02, 1.08),
            ),
            RoundingPendingCarbonReportMilestoneAdjuster(1.0),
            MinimumPendingCarbonReportMilestoneAdjuster(44.7),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 97049.7,
    )
}
