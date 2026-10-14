package com.acme.transport.lastmile.drop.cappeddropmilestone

import com.acme.transport.kit.numbers.roundTo

/** One step of the seasonal drop milestone calculation. */
interface SeasonalDropMilestoneStep {
    fun describe(): String
    fun adjust(item: SeasonalDropMilestone, value: Double): Double
}

data class RoundingSeasonalDropMilestoneAdjuster(var step: Double) : SeasonalDropMilestoneStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalDropMilestone, value: Double): Double = roundTo(value, step)
}

data class SeasonalDropMilestoneRateOffsetAdjuster(var weight: Double) : SeasonalDropMilestoneStep {
    override fun describe(): String = "plus rate times $weight"

    override fun adjust(item: SeasonalDropMilestone, value: Double): Double = value + item.rate * weight
}

data class CapSeasonalDropMilestoneAdjuster(var cap: Double) : SeasonalDropMilestoneStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalDropMilestone, value: Double): Double = minOf(value, cap)
}

data class MinimumSeasonalDropMilestoneAdjuster(var minimum: Double) : SeasonalDropMilestoneStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalDropMilestone, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredSeasonalDropMilestoneAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalDropMilestoneStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalDropMilestone, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LabelSeasonalDropMilestoneAdjuster(
    var factors: MutableMap<String, Double>,
) : SeasonalDropMilestoneStep {
    override fun describe(): String = "by label"

    override fun adjust(item: SeasonalDropMilestone, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the seasonal drop milestone steps in order and keeps a trace of each. */
data class SeasonalDropMilestoneEngine(
    var steps: MutableList<SeasonalDropMilestoneStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: SeasonalDropMilestone): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalDropMilestoneStep = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalDropMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultSeasonalDropMilestoneEngine(): SeasonalDropMilestoneEngine {
    return SeasonalDropMilestoneEngine(
        steps = mutableListOf<SeasonalDropMilestoneStep>(
            RoundingSeasonalDropMilestoneAdjuster(0.1),
            SeasonalDropMilestoneRateOffsetAdjuster(0.81),
            CapSeasonalDropMilestoneAdjuster(30394.0),
            MinimumSeasonalDropMilestoneAdjuster(2.4),
            TieredSeasonalDropMilestoneAdjuster(
                mutableListOf<Double>(1004.0, 3388.0, 4573.0),
                mutableListOf<Double>(0.99, 1.02, 1.12),
            ),
            LabelSeasonalDropMilestoneAdjuster(mutableMapOf<String, Double>("central" to 1.33, "west" to 1.29)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 46048.7,
    )
}
