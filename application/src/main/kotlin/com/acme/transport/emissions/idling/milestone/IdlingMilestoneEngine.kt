package com.acme.transport.emissions.idling.milestone

import com.acme.transport.kit.numbers.roundTo

/** One step of the idling milestone calculation. */
interface IdlingMilestoneAdjuster {
    fun describe(): String
    fun adjust(item: IdlingMilestone, value: Double): Double
}

data class TieredIdlingMilestoneAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : IdlingMilestoneAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: IdlingMilestone, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingIdlingMilestoneAdjuster(var step: Double) : IdlingMilestoneAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: IdlingMilestone, value: Double): Double = roundTo(value, step)
}

data class MinimumIdlingMilestoneAdjuster(var minimum: Double) : IdlingMilestoneAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: IdlingMilestone, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentIdlingMilestoneAdjuster(var percent: Double) : IdlingMilestoneAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: IdlingMilestone, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapIdlingMilestoneAdjuster(var cap: Double) : IdlingMilestoneAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: IdlingMilestone, value: Double): Double = minOf(value, cap)
}

/** Runs the idling milestone steps in order and keeps a trace of each. */
data class IdlingMilestoneEngine(
    var steps: MutableList<IdlingMilestoneAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: IdlingMilestone): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: IdlingMilestoneAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<IdlingMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultIdlingMilestoneEngine(): IdlingMilestoneEngine {
    return IdlingMilestoneEngine(
        steps = mutableListOf<IdlingMilestoneAdjuster>(
            TieredIdlingMilestoneAdjuster(
                mutableListOf<Double>(7556.0, 7623.0),
                mutableListOf<Double>(0.86, 1.2),
            ),
            RoundingIdlingMilestoneAdjuster(0.25),
            MinimumIdlingMilestoneAdjuster(26.8),
            PercentIdlingMilestoneAdjuster(7.9),
            CapIdlingMilestoneAdjuster(43606.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 54081.4,
    )
}
