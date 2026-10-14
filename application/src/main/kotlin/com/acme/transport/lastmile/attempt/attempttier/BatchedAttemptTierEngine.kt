package com.acme.transport.lastmile.attempt.attempttier

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the batched attempt tier calculation. */
interface BatchedAttemptTierStep {
    fun describe(): String
    fun adjust(item: BatchedAttemptTier, value: Double): Double
}

data class PercentBatchedAttemptTierAdjuster(var percent: Double) : BatchedAttemptTierStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedAttemptTier, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredBatchedAttemptTierAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedAttemptTierStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedAttemptTier, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumBatchedAttemptTierAdjuster(var minimum: Double) : BatchedAttemptTierStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedAttemptTier, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class SourceBatchedAttemptTierAdjuster(var factors: MutableMap<String, Double>) : BatchedAttemptTierStep {
    override fun describe(): String = "by source"

    override fun adjust(item: BatchedAttemptTier, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the batched attempt tier steps in order and keeps a trace of each. */
data class BatchedAttemptTierEngine(
    var steps: MutableList<BatchedAttemptTierStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: BatchedAttemptTier): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedAttemptTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultBatchedAttemptTierEngine(): BatchedAttemptTierEngine {
    return BatchedAttemptTierEngine(
        steps = mutableListOf<BatchedAttemptTierStep>(
            PercentBatchedAttemptTierAdjuster(-18.1),
            TieredBatchedAttemptTierAdjuster(
                mutableListOf<Double>(4263.0, 7308.0, 7367.0),
                mutableListOf<Double>(1.14, 1.13, 0.91),
            ),
            MinimumBatchedAttemptTierAdjuster(11.9),
            SourceBatchedAttemptTierAdjuster(
                mutableMapOf<String, Double>("west" to 1.22, "east" to 1.14, "central" to 0.72),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 21278.0,
    )
}
