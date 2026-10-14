package com.acme.transport.emissions.carbonreport.policy

import com.acme.transport.kit.numbers.roundTo

/** One step of the regional carbon report assignment calculation. */
interface RegionalCarbonReportAssignmentModifier {
    fun describe(): String
    fun adjust(item: RegionalCarbonReportAssignment, value: Double): Double
}

data class MinimumRegionalCarbonReportAssignmentAdjuster(
    var minimum: Double,
) : RegionalCarbonReportAssignmentModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalCarbonReportAssignment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentRegionalCarbonReportAssignmentAdjuster(
    var percent: Double,
) : RegionalCarbonReportAssignmentModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalCarbonReportAssignment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingRegionalCarbonReportAssignmentAdjuster(
    var step: Double,
) : RegionalCarbonReportAssignmentModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalCarbonReportAssignment, value: Double): Double = roundTo(value, step)
}

/** Runs the regional carbon report assignment steps in order and keeps a trace of each. */
data class RegionalCarbonReportAssignmentEngine(
    var steps: MutableList<RegionalCarbonReportAssignmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RegionalCarbonReportAssignment): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalCarbonReportAssignmentModifier = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalCarbonReportAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRegionalCarbonReportAssignmentEngine(): RegionalCarbonReportAssignmentEngine {
    return RegionalCarbonReportAssignmentEngine(
        steps = mutableListOf<RegionalCarbonReportAssignmentModifier>(
            MinimumRegionalCarbonReportAssignmentAdjuster(17.0),
            PercentRegionalCarbonReportAssignmentAdjuster(23.8),
            RoundingRegionalCarbonReportAssignmentAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 66331.2,
    )
}
