package com.acme.transport.lastmile.drop.dropbundle

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the batched drop budget calculation. */
interface BatchedDropBudgetStep {
    fun describe(): String
    fun adjust(item: BatchedDropBudget, value: Double): Double
}

data class PercentBatchedDropBudgetAdjuster(var percent: Double) : BatchedDropBudgetStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedDropBudget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class UnitBatchedDropBudgetAdjuster(var factors: MutableMap<String, Double>) : BatchedDropBudgetStep {
    override fun describe(): String = "by unit"

    override fun adjust(item: BatchedDropBudget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapBatchedDropBudgetAdjuster(var cap: Double) : BatchedDropBudgetStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedDropBudget, value: Double): Double = minOf(value, cap)
}

data class MinimumBatchedDropBudgetAdjuster(var minimum: Double) : BatchedDropBudgetStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedDropBudget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class BatchedDropBudgetScoreOffsetAdjuster(var weight: Double) : BatchedDropBudgetStep {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: BatchedDropBudget, value: Double): Double = value + item.score * weight
}

/** Runs the batched drop budget steps in order and keeps a trace of each. */
data class BatchedDropBudgetEngine(
    var steps: MutableList<BatchedDropBudgetStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: BatchedDropBudget): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedDropBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultBatchedDropBudgetEngine(): BatchedDropBudgetEngine {
    return BatchedDropBudgetEngine(
        steps = mutableListOf<BatchedDropBudgetStep>(
            PercentBatchedDropBudgetAdjuster(11.4),
            UnitBatchedDropBudgetAdjuster(mutableMapOf<String, Double>("central" to 1.1, "west" to 1.4)),
            CapBatchedDropBudgetAdjuster(4925.0),
            MinimumBatchedDropBudgetAdjuster(28.9),
            BatchedDropBudgetScoreOffsetAdjuster(1.57),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 44099.0,
    )
}
