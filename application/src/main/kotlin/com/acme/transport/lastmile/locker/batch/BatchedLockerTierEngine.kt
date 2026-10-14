package com.acme.transport.lastmile.locker.batch

import com.acme.transport.kit.numbers.roundTo

/** One step of the batched locker tier calculation. */
interface BatchedLockerTierFactor {
    fun describe(): String
    fun adjust(item: BatchedLockerTier, value: Double): Double
}

data class RoundingBatchedLockerTierAdjuster(var step: Double) : BatchedLockerTierFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedLockerTier, value: Double): Double = roundTo(value, step)
}

data class SegmentBatchedLockerTierAdjuster(var factors: MutableMap<String, Double>) : BatchedLockerTierFactor {
    override fun describe(): String = "by segment"

    override fun adjust(item: BatchedLockerTier, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentBatchedLockerTierAdjuster(var percent: Double) : BatchedLockerTierFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedLockerTier, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class BatchedLockerTierCreatedAtOffsetAdjuster(var weight: Double) : BatchedLockerTierFactor {
    override fun describe(): String = "plus created at times $weight"

    override fun adjust(item: BatchedLockerTier, value: Double): Double = value + item.createdAt.toDouble() * weight
}

/** Runs the batched locker tier steps in order and keeps a trace of each. */
data class BatchedLockerTierEngine(
    var steps: MutableList<BatchedLockerTierFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: BatchedLockerTier): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedLockerTierFactor = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedLockerTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultBatchedLockerTierEngine(): BatchedLockerTierEngine {
    return BatchedLockerTierEngine(
        steps = mutableListOf<BatchedLockerTierFactor>(
            RoundingBatchedLockerTierAdjuster(0.25),
            SegmentBatchedLockerTierAdjuster(
                mutableMapOf<String, Double>("north" to 0.94, "south" to 0.94, "west" to 1.18),
            ),
            PercentBatchedLockerTierAdjuster(2.8),
            BatchedLockerTierCreatedAtOffsetAdjuster(1.35),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 29627.4,
    )
}
