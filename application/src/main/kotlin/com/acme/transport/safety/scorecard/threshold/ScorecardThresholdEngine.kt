package com.acme.transport.safety.scorecard.threshold

/** One step of the scorecard threshold calculation. */
interface ScorecardThresholdModifier {
    fun describe(): String
    fun adjust(item: ScorecardThreshold, value: Double): Double
}

data class CapScorecardThresholdAdjuster(var cap: Double) : ScorecardThresholdModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScorecardThreshold, value: Double): Double = minOf(value, cap)
}

data class MinimumScorecardThresholdAdjuster(var minimum: Double) : ScorecardThresholdModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScorecardThreshold, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TitleScorecardThresholdAdjuster(
    var factors: MutableMap<String, Double>,
) : ScorecardThresholdModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: ScorecardThreshold, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the scorecard threshold steps in order and keeps a trace of each. */
data class ScorecardThresholdEngine(
    var steps: MutableList<ScorecardThresholdModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ScorecardThreshold): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScorecardThresholdModifier = steps[index]
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

    fun evaluateAll(items: MutableList<ScorecardThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultScorecardThresholdEngine(): ScorecardThresholdEngine {
    return ScorecardThresholdEngine(
        steps = mutableListOf<ScorecardThresholdModifier>(
            CapScorecardThresholdAdjuster(15731.0),
            MinimumScorecardThresholdAdjuster(46.3),
            TitleScorecardThresholdAdjuster(mutableMapOf<String, Double>("west" to 1.12, "north" to 1.19)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 53303.7,
    )
}
