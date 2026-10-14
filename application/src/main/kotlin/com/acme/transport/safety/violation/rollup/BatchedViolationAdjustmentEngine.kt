package com.acme.transport.safety.violation.rollup

import com.acme.transport.kit.numbers.roundTo

/** One step of the batched violation adjustment calculation. */
interface BatchedViolationAdjustmentModifier {
    fun describe(): String
    fun adjust(item: BatchedViolationAdjustment, value: Double): Double
}

data class NameBatchedViolationAdjustmentAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedViolationAdjustmentModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: BatchedViolationAdjustment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingBatchedViolationAdjustmentAdjuster(var step: Double) : BatchedViolationAdjustmentModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedViolationAdjustment, value: Double): Double = roundTo(value, step)
}

data class MinimumBatchedViolationAdjustmentAdjuster(var minimum: Double) : BatchedViolationAdjustmentModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedViolationAdjustment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the batched violation adjustment steps in order and keeps a trace of each. */
data class BatchedViolationAdjustmentEngine(
    var steps: MutableList<BatchedViolationAdjustmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: BatchedViolationAdjustment): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedViolationAdjustmentModifier = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedViolationAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultBatchedViolationAdjustmentEngine(): BatchedViolationAdjustmentEngine {
    return BatchedViolationAdjustmentEngine(
        steps = mutableListOf<BatchedViolationAdjustmentModifier>(
            NameBatchedViolationAdjustmentAdjuster(
                mutableMapOf<String, Double>("retail" to 1.17, "south" to 0.73, "east" to 0.78),
            ),
            RoundingBatchedViolationAdjustmentAdjuster(0.05),
            MinimumBatchedViolationAdjustmentAdjuster(24.4),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 95402.9,
    )
}
