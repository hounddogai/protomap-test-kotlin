package com.acme.transport.customs.hold.request

/** One step of the batched hold allocation calculation. */
interface BatchedHoldAllocationAdjuster {
    fun describe(): String
    fun adjust(item: BatchedHoldAllocation, value: Double): Double
}

data class CapBatchedHoldAllocationAdjuster(var cap: Double) : BatchedHoldAllocationAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedHoldAllocation, value: Double): Double = minOf(value, cap)
}

data class SourceBatchedHoldAllocationAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedHoldAllocationAdjuster {
    override fun describe(): String = "by source"

    override fun adjust(item: BatchedHoldAllocation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredBatchedHoldAllocationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedHoldAllocationAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedHoldAllocation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumBatchedHoldAllocationAdjuster(var minimum: Double) : BatchedHoldAllocationAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedHoldAllocation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the batched hold allocation steps in order and keeps a trace of each. */
data class BatchedHoldAllocationEngine(
    var steps: MutableList<BatchedHoldAllocationAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: BatchedHoldAllocation): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedHoldAllocationAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedHoldAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultBatchedHoldAllocationEngine(): BatchedHoldAllocationEngine {
    return BatchedHoldAllocationEngine(
        steps = mutableListOf<BatchedHoldAllocationAdjuster>(
            CapBatchedHoldAllocationAdjuster(42283.0),
            SourceBatchedHoldAllocationAdjuster(mutableMapOf<String, Double>("north" to 0.75, "west" to 0.95)),
            TieredBatchedHoldAllocationAdjuster(
                mutableListOf<Double>(2091.0, 2531.0, 5283.0, 9156.0),
                mutableListOf<Double>(1.2, 0.94, 0.84, 1.18),
            ),
            MinimumBatchedHoldAllocationAdjuster(6.3),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 52650.8,
    )
}
