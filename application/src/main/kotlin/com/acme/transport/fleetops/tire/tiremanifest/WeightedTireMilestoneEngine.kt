package com.acme.transport.fleetops.tire.tiremanifest

import com.acme.transport.kit.numbers.roundTo

/** One step of the weighted tire milestone calculation. */
interface WeightedTireMilestoneModifier {
    fun describe(): String
    fun adjust(item: WeightedTireMilestone, value: Double): Double
}

data class MinimumWeightedTireMilestoneAdjuster(var minimum: Double) : WeightedTireMilestoneModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedTireMilestone, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapWeightedTireMilestoneAdjuster(var cap: Double) : WeightedTireMilestoneModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedTireMilestone, value: Double): Double = minOf(value, cap)
}

data class RoundingWeightedTireMilestoneAdjuster(var step: Double) : WeightedTireMilestoneModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedTireMilestone, value: Double): Double = roundTo(value, step)
}

data class PercentWeightedTireMilestoneAdjuster(var percent: Double) : WeightedTireMilestoneModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedTireMilestone, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the weighted tire milestone steps in order and keeps a trace of each. */
data class WeightedTireMilestoneEngine(
    var steps: MutableList<WeightedTireMilestoneModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: WeightedTireMilestone): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedTireMilestoneModifier = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedTireMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultWeightedTireMilestoneEngine(): WeightedTireMilestoneEngine {
    return WeightedTireMilestoneEngine(
        steps = mutableListOf<WeightedTireMilestoneModifier>(
            MinimumWeightedTireMilestoneAdjuster(12.0),
            CapWeightedTireMilestoneAdjuster(25595.0),
            RoundingWeightedTireMilestoneAdjuster(1.0),
            PercentWeightedTireMilestoneAdjuster(-0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 38040.3,
    )
}
