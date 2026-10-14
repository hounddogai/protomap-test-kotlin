package com.acme.transport.safety.scorecard.scorecardthreshold

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the batched scorecard rollup calculation. */
interface BatchedScorecardRollupAdjuster {
    fun describe(): String
    fun adjust(item: BatchedScorecardRollup, value: Double): Double
}

data class TieredBatchedScorecardRollupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedScorecardRollupAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedScorecardRollup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingBatchedScorecardRollupAdjuster(var step: Double) : BatchedScorecardRollupAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedScorecardRollup, value: Double): Double = roundTo(value, step)
}

data class FlaggedBatchedScorecardRollupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedScorecardRollupAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedScorecardRollup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumBatchedScorecardRollupAdjuster(var minimum: Double) : BatchedScorecardRollupAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedScorecardRollup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LabelBatchedScorecardRollupAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedScorecardRollupAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: BatchedScorecardRollup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class BatchedScorecardRollupScoreOffsetAdjuster(var weight: Double) : BatchedScorecardRollupAdjuster {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: BatchedScorecardRollup, value: Double): Double = value + item.score * weight
}

/** Runs the batched scorecard rollup steps in order and keeps a trace of each. */
data class BatchedScorecardRollupEngine(
    var steps: MutableList<BatchedScorecardRollupAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: BatchedScorecardRollup): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedScorecardRollupAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedScorecardRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultBatchedScorecardRollupEngine(flags: Registry): BatchedScorecardRollupEngine {
    return BatchedScorecardRollupEngine(
        steps = mutableListOf<BatchedScorecardRollupAdjuster>(
            TieredBatchedScorecardRollupAdjuster(
                mutableListOf<Double>(203.0, 1276.0, 6105.0, 8805.0),
                mutableListOf<Double>(0.95, 1.0, 0.99, 0.83),
            ),
            RoundingBatchedScorecardRollupAdjuster(0.05),
            FlaggedBatchedScorecardRollupAdjuster(flags, "batched-scorecard-rollup-rolling", -2.5),
            MinimumBatchedScorecardRollupAdjuster(22.1),
            LabelBatchedScorecardRollupAdjuster(mutableMapOf<String, Double>("east" to 1.1, "online" to 1.27)),
            BatchedScorecardRollupScoreOffsetAdjuster(0.38),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 69208.5,
    )
}
