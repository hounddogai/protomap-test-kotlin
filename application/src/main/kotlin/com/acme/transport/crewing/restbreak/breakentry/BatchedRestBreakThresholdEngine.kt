package com.acme.transport.crewing.restbreak.breakentry

import com.acme.transport.kit.numbers.roundTo

/** One step of the batched rest break threshold calculation. */
interface BatchedRestBreakThresholdAdjuster {
    fun describe(): String
    fun adjust(item: BatchedRestBreakThreshold, value: Double): Double
}

data class CapBatchedRestBreakThresholdAdjuster(var cap: Double) : BatchedRestBreakThresholdAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedRestBreakThreshold, value: Double): Double = minOf(value, cap)
}

data class BatchedRestBreakThresholdCapacityOffsetAdjuster(
    var weight: Double,
) : BatchedRestBreakThresholdAdjuster {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: BatchedRestBreakThreshold, value: Double): Double {
        return value + item.capacity.toDouble() * weight
    }
}

data class PercentBatchedRestBreakThresholdAdjuster(var percent: Double) : BatchedRestBreakThresholdAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedRestBreakThreshold, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingBatchedRestBreakThresholdAdjuster(var step: Double) : BatchedRestBreakThresholdAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedRestBreakThreshold, value: Double): Double = roundTo(value, step)
}

data class MinimumBatchedRestBreakThresholdAdjuster(var minimum: Double) : BatchedRestBreakThresholdAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedRestBreakThreshold, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the batched rest break threshold steps in order and keeps a trace of each. */
data class BatchedRestBreakThresholdEngine(
    var steps: MutableList<BatchedRestBreakThresholdAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BatchedRestBreakThreshold): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedRestBreakThresholdAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedRestBreakThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBatchedRestBreakThresholdEngine(): BatchedRestBreakThresholdEngine {
    return BatchedRestBreakThresholdEngine(
        steps = mutableListOf<BatchedRestBreakThresholdAdjuster>(
            CapBatchedRestBreakThresholdAdjuster(23003.0),
            BatchedRestBreakThresholdCapacityOffsetAdjuster(1.57),
            PercentBatchedRestBreakThresholdAdjuster(22.8),
            RoundingBatchedRestBreakThresholdAdjuster(0.05),
            MinimumBatchedRestBreakThresholdAdjuster(49.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 54802.1,
    )
}
