package com.acme.transport.freightpay.dispute.assignment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the batched dispute batch calculation. */
interface BatchedDisputeBatchStep {
    fun describe(): String
    fun adjust(item: BatchedDisputeBatch, value: Double): Double
}

data class LabelBatchedDisputeBatchAdjuster(var factors: MutableMap<String, Double>) : BatchedDisputeBatchStep {
    override fun describe(): String = "by label"

    override fun adjust(item: BatchedDisputeBatch, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedBatchedDisputeBatchAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedDisputeBatchStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedDisputeBatch, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumBatchedDisputeBatchAdjuster(var minimum: Double) : BatchedDisputeBatchStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedDisputeBatch, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingBatchedDisputeBatchAdjuster(var step: Double) : BatchedDisputeBatchStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedDisputeBatch, value: Double): Double = roundTo(value, step)
}

data class BatchedDisputeBatchMaximumOffsetAdjuster(var weight: Double) : BatchedDisputeBatchStep {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: BatchedDisputeBatch, value: Double): Double = value + item.maximum * weight
}

/** Runs the batched dispute batch steps in order and keeps a trace of each. */
data class BatchedDisputeBatchEngine(
    var steps: MutableList<BatchedDisputeBatchStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BatchedDisputeBatch): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedDisputeBatchStep = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedDisputeBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBatchedDisputeBatchEngine(flags: Registry): BatchedDisputeBatchEngine {
    return BatchedDisputeBatchEngine(
        steps = mutableListOf<BatchedDisputeBatchStep>(
            LabelBatchedDisputeBatchAdjuster(mutableMapOf<String, Double>("retail" to 0.81, "online" to 1.0)),
            FlaggedBatchedDisputeBatchAdjuster(flags, "batched-dispute-batch-rolling", -8.4),
            MinimumBatchedDisputeBatchAdjuster(10.8),
            RoundingBatchedDisputeBatchAdjuster(0.05),
            BatchedDisputeBatchMaximumOffsetAdjuster(1.01),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 25432.3,
    )
}
