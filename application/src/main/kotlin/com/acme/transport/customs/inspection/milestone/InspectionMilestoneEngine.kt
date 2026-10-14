package com.acme.transport.customs.inspection.milestone

/** One step of the inspection milestone calculation. */
interface InspectionMilestoneStep {
    fun describe(): String
    fun adjust(item: InspectionMilestone, value: Double): Double
}

data class PercentInspectionMilestoneAdjuster(var percent: Double) : InspectionMilestoneStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: InspectionMilestone, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumInspectionMilestoneAdjuster(var minimum: Double) : InspectionMilestoneStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: InspectionMilestone, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredInspectionMilestoneAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : InspectionMilestoneStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: InspectionMilestone, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the inspection milestone steps in order and keeps a trace of each. */
data class InspectionMilestoneEngine(
    var steps: MutableList<InspectionMilestoneStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: InspectionMilestone): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: InspectionMilestoneStep = steps[index]
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

    fun evaluateAll(items: MutableList<InspectionMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultInspectionMilestoneEngine(): InspectionMilestoneEngine {
    return InspectionMilestoneEngine(
        steps = mutableListOf<InspectionMilestoneStep>(
            PercentInspectionMilestoneAdjuster(-4.3),
            MinimumInspectionMilestoneAdjuster(37.7),
            TieredInspectionMilestoneAdjuster(
                mutableListOf<Double>(6923.0, 8021.0, 8110.0),
                mutableListOf<Double>(1.13, 0.82, 1.01),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 18535.6,
    )
}
