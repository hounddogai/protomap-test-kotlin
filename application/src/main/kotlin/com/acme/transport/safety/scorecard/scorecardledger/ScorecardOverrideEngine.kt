package com.acme.transport.safety.scorecard.scorecardledger

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the scorecard override calculation. */
interface ScorecardOverrideStep {
    fun describe(): String
    fun adjust(item: ScorecardOverride, value: Double): Double
}

data class ScorecardOverrideMaximumOffsetAdjuster(var weight: Double) : ScorecardOverrideStep {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: ScorecardOverride, value: Double): Double = value + item.maximum * weight
}

data class RoundingScorecardOverrideAdjuster(var step: Double) : ScorecardOverrideStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScorecardOverride, value: Double): Double = roundTo(value, step)
}

data class MinimumScorecardOverrideAdjuster(var minimum: Double) : ScorecardOverrideStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScorecardOverride, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TitleScorecardOverrideAdjuster(var factors: MutableMap<String, Double>) : ScorecardOverrideStep {
    override fun describe(): String = "by title"

    override fun adjust(item: ScorecardOverride, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredScorecardOverrideAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScorecardOverrideStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScorecardOverride, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedScorecardOverrideAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScorecardOverrideStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScorecardOverride, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the scorecard override steps in order and keeps a trace of each. */
data class ScorecardOverrideEngine(
    var steps: MutableList<ScorecardOverrideStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ScorecardOverride): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScorecardOverrideStep = steps[index]
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

    fun evaluateAll(items: MutableList<ScorecardOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultScorecardOverrideEngine(flags: Registry): ScorecardOverrideEngine {
    return ScorecardOverrideEngine(
        steps = mutableListOf<ScorecardOverrideStep>(
            ScorecardOverrideMaximumOffsetAdjuster(1.65),
            RoundingScorecardOverrideAdjuster(0.5),
            MinimumScorecardOverrideAdjuster(2.0),
            TitleScorecardOverrideAdjuster(mutableMapOf<String, Double>("retail" to 1.29, "online" to 1.37)),
            TieredScorecardOverrideAdjuster(
                mutableListOf<Double>(815.0, 1624.0),
                mutableListOf<Double>(1.0, 1.17),
            ),
            FlaggedScorecardOverrideAdjuster(flags, "scorecard-override-default", -2.7),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 49725.1,
    )
}
