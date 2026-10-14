package com.acme.transport.safety.scorecard.scorecardbudget

import com.acme.transport.kit.flags.Registry

/** One step of the batched scorecard budget calculation. */
interface BatchedScorecardBudgetModifier {
    fun describe(): String
    fun adjust(item: BatchedScorecardBudget, value: Double): Double
}

data class FlaggedBatchedScorecardBudgetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedScorecardBudgetModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedScorecardBudget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class BatchedScorecardBudgetDiscountOffsetAdjuster(var weight: Double) : BatchedScorecardBudgetModifier {
    override fun describe(): String = "plus discount times $weight"

    override fun adjust(item: BatchedScorecardBudget, value: Double): Double = value + item.discount * weight
}

data class PercentBatchedScorecardBudgetAdjuster(var percent: Double) : BatchedScorecardBudgetModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedScorecardBudget, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the batched scorecard budget steps in order and keeps a trace of each. */
data class BatchedScorecardBudgetEngine(
    var steps: MutableList<BatchedScorecardBudgetModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: BatchedScorecardBudget): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedScorecardBudgetModifier = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedScorecardBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultBatchedScorecardBudgetEngine(flags: Registry): BatchedScorecardBudgetEngine {
    return BatchedScorecardBudgetEngine(
        steps = mutableListOf<BatchedScorecardBudgetModifier>(
            FlaggedBatchedScorecardBudgetAdjuster(flags, "batched-scorecard-budget-pending", 7.5),
            BatchedScorecardBudgetDiscountOffsetAdjuster(1.22),
            PercentBatchedScorecardBudgetAdjuster(-22.7),
        ),
        trace = mutableListOf<String>(),
        floor = 0.0,
        ceiling = 72973.4,
    )
}
