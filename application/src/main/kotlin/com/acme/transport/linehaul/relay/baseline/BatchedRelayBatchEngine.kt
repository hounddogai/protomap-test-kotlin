package com.acme.transport.linehaul.relay.baseline

import com.acme.transport.kit.numbers.roundTo

/** One step of the batched relay batch calculation. */
interface BatchedRelayBatchAdjuster {
    fun describe(): String
    fun adjust(item: BatchedRelayBatch, value: Double): Double
}

data class RoundingBatchedRelayBatchAdjuster(var step: Double) : BatchedRelayBatchAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedRelayBatch, value: Double): Double = roundTo(value, step)
}

data class NameBatchedRelayBatchAdjuster(var factors: MutableMap<String, Double>) : BatchedRelayBatchAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: BatchedRelayBatch, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentBatchedRelayBatchAdjuster(var percent: Double) : BatchedRelayBatchAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedRelayBatch, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class BatchedRelayBatchBudgetOffsetAdjuster(var weight: Double) : BatchedRelayBatchAdjuster {
    override fun describe(): String = "plus budget times $weight"

    override fun adjust(item: BatchedRelayBatch, value: Double): Double = value + item.budget * weight
}

/** Runs the batched relay batch steps in order and keeps a trace of each. */
data class BatchedRelayBatchEngine(
    var steps: MutableList<BatchedRelayBatchAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: BatchedRelayBatch): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedRelayBatchAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedRelayBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultBatchedRelayBatchEngine(): BatchedRelayBatchEngine {
    return BatchedRelayBatchEngine(
        steps = mutableListOf<BatchedRelayBatchAdjuster>(
            RoundingBatchedRelayBatchAdjuster(0.5),
            NameBatchedRelayBatchAdjuster(mutableMapOf<String, Double>("west" to 1.23, "north" to 1.27)),
            PercentBatchedRelayBatchAdjuster(22.5),
            BatchedRelayBatchBudgetOffsetAdjuster(0.44),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 96280.6,
    )
}
