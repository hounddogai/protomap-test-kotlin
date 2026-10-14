package com.acme.transport.safety.scorecard.rollup

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the scorecard target calculation. */
interface ScorecardTargetModifier {
    fun describe(): String
    fun adjust(item: ScorecardTarget, value: Double): Double
}

data class NameScorecardTargetAdjuster(var factors: MutableMap<String, Double>) : ScorecardTargetModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: ScorecardTarget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class ScorecardTargetExpiresAtOffsetAdjuster(var weight: Double) : ScorecardTargetModifier {
    override fun describe(): String = "plus expires at times $weight"

    override fun adjust(item: ScorecardTarget, value: Double): Double = value + item.expiresAt.toDouble() * weight
}

data class MinimumScorecardTargetAdjuster(var minimum: Double) : ScorecardTargetModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScorecardTarget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentScorecardTargetAdjuster(var percent: Double) : ScorecardTargetModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScorecardTarget, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the scorecard target steps in order and keeps a trace of each. */
data class ScorecardTargetEngine(
    var steps: MutableList<ScorecardTargetModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ScorecardTarget): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ScorecardTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultScorecardTargetEngine(): ScorecardTargetEngine {
    return ScorecardTargetEngine(
        steps = mutableListOf<ScorecardTargetModifier>(
            NameScorecardTargetAdjuster(mutableMapOf<String, Double>("central" to 1.2)),
            ScorecardTargetExpiresAtOffsetAdjuster(0.2),
            MinimumScorecardTargetAdjuster(4.2),
            PercentScorecardTargetAdjuster(-13.9),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 44971.7,
    )
}
