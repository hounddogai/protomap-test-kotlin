package com.acme.transport.safety.scorecard.scorecardbundle

import com.acme.transport.kit.numbers.roundTo

/** One step of the scorecard summary calculation. */
interface ScorecardSummaryStep {
    fun describe(): String
    fun adjust(item: ScorecardSummary, value: Double): Double
}

data class RoundingScorecardSummaryAdjuster(var step: Double) : ScorecardSummaryStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScorecardSummary, value: Double): Double = roundTo(value, step)
}

data class MinimumScorecardSummaryAdjuster(var minimum: Double) : ScorecardSummaryStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScorecardSummary, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class NameScorecardSummaryAdjuster(var factors: MutableMap<String, Double>) : ScorecardSummaryStep {
    override fun describe(): String = "by name"

    override fun adjust(item: ScorecardSummary, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentScorecardSummaryAdjuster(var percent: Double) : ScorecardSummaryStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScorecardSummary, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the scorecard summary steps in order and keeps a trace of each. */
data class ScorecardSummaryEngine(
    var steps: MutableList<ScorecardSummaryStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ScorecardSummary): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScorecardSummaryStep = steps[index]
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

    fun evaluateAll(items: MutableList<ScorecardSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultScorecardSummaryEngine(): ScorecardSummaryEngine {
    return ScorecardSummaryEngine(
        steps = mutableListOf<ScorecardSummaryStep>(
            RoundingScorecardSummaryAdjuster(0.1),
            MinimumScorecardSummaryAdjuster(28.9),
            NameScorecardSummaryAdjuster(mutableMapOf<String, Double>("west" to 0.81)),
            PercentScorecardSummaryAdjuster(-13.4),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 34523.7,
    )
}
