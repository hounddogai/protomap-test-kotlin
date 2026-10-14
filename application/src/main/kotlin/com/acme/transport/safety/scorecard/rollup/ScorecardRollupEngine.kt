package com.acme.transport.safety.scorecard.rollup

import com.acme.transport.kit.numbers.roundTo

/** One step of the scorecard rollup calculation. */
interface ScorecardRollupAdjuster {
    fun describe(): String
    fun adjust(item: ScorecardRollup, value: Double): Double
}

data class RoundingScorecardRollupAdjuster(var step: Double) : ScorecardRollupAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScorecardRollup, value: Double): Double = roundTo(value, step)
}

data class MinimumScorecardRollupAdjuster(var minimum: Double) : ScorecardRollupAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScorecardRollup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LabelScorecardRollupAdjuster(var factors: MutableMap<String, Double>) : ScorecardRollupAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: ScorecardRollup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the scorecard rollup steps in order and keeps a trace of each. */
data class ScorecardRollupEngine(
    var steps: MutableList<ScorecardRollupAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ScorecardRollup): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScorecardRollupAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ScorecardRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultScorecardRollupEngine(): ScorecardRollupEngine {
    return ScorecardRollupEngine(
        steps = mutableListOf<ScorecardRollupAdjuster>(
            RoundingScorecardRollupAdjuster(1.0),
            MinimumScorecardRollupAdjuster(4.4),
            LabelScorecardRollupAdjuster(mutableMapOf<String, Double>("west" to 0.94)),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 17061.9,
    )
}
