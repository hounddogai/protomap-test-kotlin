package com.acme.transport.safety.scorecard.scorecardslot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the scorecard preference calculation. */
interface ScorecardPreferenceAdjuster {
    fun describe(): String
    fun adjust(item: ScorecardPreference, value: Double): Double
}

data class LocaleScorecardPreferenceAdjuster(
    var factors: MutableMap<String, Double>,
) : ScorecardPreferenceAdjuster {
    override fun describe(): String = "by locale"

    override fun adjust(item: ScorecardPreference, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class ScorecardPreferenceUpdatedAtOffsetAdjuster(var weight: Double) : ScorecardPreferenceAdjuster {
    override fun describe(): String = "plus updated at times $weight"

    override fun adjust(item: ScorecardPreference, value: Double): Double = value + item.updatedAt.toDouble() * weight
}

data class FlaggedScorecardPreferenceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScorecardPreferenceAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScorecardPreference, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentScorecardPreferenceAdjuster(var percent: Double) : ScorecardPreferenceAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScorecardPreference, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapScorecardPreferenceAdjuster(var cap: Double) : ScorecardPreferenceAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScorecardPreference, value: Double): Double = minOf(value, cap)
}

data class RoundingScorecardPreferenceAdjuster(var step: Double) : ScorecardPreferenceAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScorecardPreference, value: Double): Double = roundTo(value, step)
}

/** Runs the scorecard preference steps in order and keeps a trace of each. */
data class ScorecardPreferenceEngine(
    var steps: MutableList<ScorecardPreferenceAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ScorecardPreference): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScorecardPreferenceAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ScorecardPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultScorecardPreferenceEngine(flags: Registry): ScorecardPreferenceEngine {
    return ScorecardPreferenceEngine(
        steps = mutableListOf<ScorecardPreferenceAdjuster>(
            LocaleScorecardPreferenceAdjuster(mutableMapOf<String, Double>("west" to 1.22)),
            ScorecardPreferenceUpdatedAtOffsetAdjuster(1.35),
            FlaggedScorecardPreferenceAdjuster(flags, "scorecard-preference-rolling", 6.7),
            PercentScorecardPreferenceAdjuster(-13.7),
            CapScorecardPreferenceAdjuster(16931.0),
            RoundingScorecardPreferenceAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 4741.4,
    )
}
