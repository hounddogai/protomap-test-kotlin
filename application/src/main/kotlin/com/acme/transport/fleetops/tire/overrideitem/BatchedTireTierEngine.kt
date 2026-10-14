package com.acme.transport.fleetops.tire.overrideitem

import com.acme.transport.kit.numbers.roundTo

/** One step of the batched tire tier calculation. */
interface BatchedTireTierAdjuster {
    fun describe(): String
    fun adjust(item: BatchedTireTier, value: Double): Double
}

data class MinimumBatchedTireTierAdjuster(var minimum: Double) : BatchedTireTierAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedTireTier, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapBatchedTireTierAdjuster(var cap: Double) : BatchedTireTierAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedTireTier, value: Double): Double = minOf(value, cap)
}

data class DepotBatchedTireTierAdjuster(var factors: MutableMap<String, Double>) : BatchedTireTierAdjuster {
    override fun describe(): String = "by depot"

    override fun adjust(item: BatchedTireTier, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingBatchedTireTierAdjuster(var step: Double) : BatchedTireTierAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedTireTier, value: Double): Double = roundTo(value, step)
}

/** Runs the batched tire tier steps in order and keeps a trace of each. */
data class BatchedTireTierEngine(
    var steps: MutableList<BatchedTireTierAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: BatchedTireTier): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedTireTierAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedTireTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultBatchedTireTierEngine(): BatchedTireTierEngine {
    return BatchedTireTierEngine(
        steps = mutableListOf<BatchedTireTierAdjuster>(
            MinimumBatchedTireTierAdjuster(29.6),
            CapBatchedTireTierAdjuster(49574.0),
            DepotBatchedTireTierAdjuster(mutableMapOf<String, Double>("east" to 0.92, "online" to 1.02)),
            RoundingBatchedTireTierAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 12118.2,
    )
}
