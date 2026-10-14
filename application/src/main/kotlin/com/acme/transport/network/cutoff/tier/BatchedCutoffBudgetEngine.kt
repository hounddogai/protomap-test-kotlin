package com.acme.transport.network.cutoff.tier

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the batched cutoff budget calculation. */
interface BatchedCutoffBudgetAdjuster {
    fun describe(): String
    fun adjust(item: BatchedCutoffBudget, value: Double): Double
}

data class PercentBatchedCutoffBudgetAdjuster(var percent: Double) : BatchedCutoffBudgetAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedCutoffBudget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumBatchedCutoffBudgetAdjuster(var minimum: Double) : BatchedCutoffBudgetAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedCutoffBudget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapBatchedCutoffBudgetAdjuster(var cap: Double) : BatchedCutoffBudgetAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedCutoffBudget, value: Double): Double = minOf(value, cap)
}

data class RoundingBatchedCutoffBudgetAdjuster(var step: Double) : BatchedCutoffBudgetAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedCutoffBudget, value: Double): Double = roundTo(value, step)
}

data class LocaleBatchedCutoffBudgetAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedCutoffBudgetAdjuster {
    override fun describe(): String = "by locale"

    override fun adjust(item: BatchedCutoffBudget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedBatchedCutoffBudgetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedCutoffBudgetAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedCutoffBudget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the batched cutoff budget steps in order and keeps a trace of each. */
data class BatchedCutoffBudgetEngine(
    var steps: MutableList<BatchedCutoffBudgetAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: BatchedCutoffBudget): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedCutoffBudgetAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedCutoffBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultBatchedCutoffBudgetEngine(flags: Registry): BatchedCutoffBudgetEngine {
    return BatchedCutoffBudgetEngine(
        steps = mutableListOf<BatchedCutoffBudgetAdjuster>(
            PercentBatchedCutoffBudgetAdjuster(-8.0),
            MinimumBatchedCutoffBudgetAdjuster(10.4),
            CapBatchedCutoffBudgetAdjuster(17171.0),
            RoundingBatchedCutoffBudgetAdjuster(0.25),
            LocaleBatchedCutoffBudgetAdjuster(
                mutableMapOf<String, Double>("central" to 1.08, "east" to 1.28, "north" to 0.88),
            ),
            FlaggedBatchedCutoffBudgetAdjuster(flags, "batched-cutoff-budget-batched", -4.7),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 82026.8,
    )
}
