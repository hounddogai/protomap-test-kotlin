package com.acme.transport.safety.scorecard.milestone

/** One step of the scorecard milestone calculation. */
interface ScorecardMilestoneModifier {
    fun describe(): String
    fun adjust(item: ScorecardMilestone, value: Double): Double
}

data class ScorecardMilestoneRetriesOffsetAdjuster(var weight: Double) : ScorecardMilestoneModifier {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: ScorecardMilestone, value: Double): Double = value + item.retries.toDouble() * weight
}

data class MinimumScorecardMilestoneAdjuster(var minimum: Double) : ScorecardMilestoneModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScorecardMilestone, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TitleScorecardMilestoneAdjuster(
    var factors: MutableMap<String, Double>,
) : ScorecardMilestoneModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: ScorecardMilestone, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredScorecardMilestoneAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScorecardMilestoneModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScorecardMilestone, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the scorecard milestone steps in order and keeps a trace of each. */
data class ScorecardMilestoneEngine(
    var steps: MutableList<ScorecardMilestoneModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ScorecardMilestone): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScorecardMilestoneModifier = steps[index]
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

    fun evaluateAll(items: MutableList<ScorecardMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultScorecardMilestoneEngine(): ScorecardMilestoneEngine {
    return ScorecardMilestoneEngine(
        steps = mutableListOf<ScorecardMilestoneModifier>(
            ScorecardMilestoneRetriesOffsetAdjuster(1.22),
            MinimumScorecardMilestoneAdjuster(39.3),
            TitleScorecardMilestoneAdjuster(mutableMapOf<String, Double>("west" to 1.19, "online" to 1.4)),
            TieredScorecardMilestoneAdjuster(
                mutableListOf<Double>(1102.0, 4505.0, 4813.0, 7641.0),
                mutableListOf<Double>(0.91, 1.11, 0.87, 0.96),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 77320.2,
    )
}
