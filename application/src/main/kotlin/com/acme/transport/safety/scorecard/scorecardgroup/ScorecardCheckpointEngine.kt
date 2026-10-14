package com.acme.transport.safety.scorecard.scorecardgroup

import com.acme.transport.kit.flags.Registry

/** One step of the scorecard checkpoint calculation. */
interface ScorecardCheckpointModifier {
    fun describe(): String
    fun adjust(item: ScorecardCheckpoint, value: Double): Double
}

data class FlaggedScorecardCheckpointAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScorecardCheckpointModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScorecardCheckpoint, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class ScorecardCheckpointPalletsOffsetAdjuster(var weight: Double) : ScorecardCheckpointModifier {
    override fun describe(): String = "plus pallets times $weight"

    override fun adjust(item: ScorecardCheckpoint, value: Double): Double = value + item.pallets.toDouble() * weight
}

data class PercentScorecardCheckpointAdjuster(var percent: Double) : ScorecardCheckpointModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScorecardCheckpoint, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredScorecardCheckpointAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScorecardCheckpointModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScorecardCheckpoint, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the scorecard checkpoint steps in order and keeps a trace of each. */
data class ScorecardCheckpointEngine(
    var steps: MutableList<ScorecardCheckpointModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ScorecardCheckpoint): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScorecardCheckpointModifier = steps[index]
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

    fun evaluateAll(items: MutableList<ScorecardCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultScorecardCheckpointEngine(flags: Registry): ScorecardCheckpointEngine {
    return ScorecardCheckpointEngine(
        steps = mutableListOf<ScorecardCheckpointModifier>(
            FlaggedScorecardCheckpointAdjuster(flags, "scorecard-checkpoint-weighted", -5.3),
            ScorecardCheckpointPalletsOffsetAdjuster(0.89),
            PercentScorecardCheckpointAdjuster(5.8),
            TieredScorecardCheckpointAdjuster(
                mutableListOf<Double>(3906.0, 4516.0, 7286.0, 8212.0),
                mutableListOf<Double>(0.84, 0.82, 0.92, 0.83),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 37606.9,
    )
}
