package com.acme.transport.safety.scorecard.scorecardwindow

import com.acme.transport.kit.numbers.roundTo

/** One step of the batched scorecard threshold calculation. */
interface BatchedScorecardThresholdFactor {
    fun describe(): String
    fun adjust(item: BatchedScorecardThreshold, value: Double): Double
}

data class BatchedScorecardThresholdRevisionOffsetAdjuster(
    var weight: Double,
) : BatchedScorecardThresholdFactor {
    override fun describe(): String = "plus revision times $weight"

    override fun adjust(item: BatchedScorecardThreshold, value: Double): Double {
        return value + item.revision.toDouble() * weight
    }
}

data class CapBatchedScorecardThresholdAdjuster(var cap: Double) : BatchedScorecardThresholdFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedScorecardThreshold, value: Double): Double = minOf(value, cap)
}

data class PercentBatchedScorecardThresholdAdjuster(var percent: Double) : BatchedScorecardThresholdFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedScorecardThreshold, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredBatchedScorecardThresholdAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedScorecardThresholdFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedScorecardThreshold, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumBatchedScorecardThresholdAdjuster(var minimum: Double) : BatchedScorecardThresholdFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedScorecardThreshold, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingBatchedScorecardThresholdAdjuster(var step: Double) : BatchedScorecardThresholdFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedScorecardThreshold, value: Double): Double = roundTo(value, step)
}

/** Runs the batched scorecard threshold steps in order and keeps a trace of each. */
data class BatchedScorecardThresholdEngine(
    var steps: MutableList<BatchedScorecardThresholdFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: BatchedScorecardThreshold): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedScorecardThresholdFactor = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedScorecardThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultBatchedScorecardThresholdEngine(): BatchedScorecardThresholdEngine {
    return BatchedScorecardThresholdEngine(
        steps = mutableListOf<BatchedScorecardThresholdFactor>(
            BatchedScorecardThresholdRevisionOffsetAdjuster(1.15),
            CapBatchedScorecardThresholdAdjuster(28483.0),
            PercentBatchedScorecardThresholdAdjuster(12.2),
            TieredBatchedScorecardThresholdAdjuster(
                mutableListOf<Double>(2157.0, 5657.0, 9152.0),
                mutableListOf<Double>(1.17, 1.08, 0.81),
            ),
            MinimumBatchedScorecardThresholdAdjuster(13.9),
            RoundingBatchedScorecardThresholdAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 80557.2,
    )
}
