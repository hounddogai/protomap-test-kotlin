package com.acme.transport.safety.scorecard.tieredscorecardslot

import com.acme.transport.kit.flags.Registry

/** One step of the batched scorecard plan calculation. */
interface BatchedScorecardPlanStep {
    fun describe(): String
    fun adjust(item: BatchedScorecardPlan, value: Double): Double
}

data class BatchedScorecardPlanMarginOffsetAdjuster(var weight: Double) : BatchedScorecardPlanStep {
    override fun describe(): String = "plus margin times $weight"

    override fun adjust(item: BatchedScorecardPlan, value: Double): Double = value + item.margin * weight
}

data class PercentBatchedScorecardPlanAdjuster(var percent: Double) : BatchedScorecardPlanStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedScorecardPlan, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedBatchedScorecardPlanAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedScorecardPlanStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedScorecardPlan, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumBatchedScorecardPlanAdjuster(var minimum: Double) : BatchedScorecardPlanStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedScorecardPlan, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapBatchedScorecardPlanAdjuster(var cap: Double) : BatchedScorecardPlanStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedScorecardPlan, value: Double): Double = minOf(value, cap)
}

/** Runs the batched scorecard plan steps in order and keeps a trace of each. */
data class BatchedScorecardPlanEngine(
    var steps: MutableList<BatchedScorecardPlanStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: BatchedScorecardPlan): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedScorecardPlanStep = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedScorecardPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultBatchedScorecardPlanEngine(flags: Registry): BatchedScorecardPlanEngine {
    return BatchedScorecardPlanEngine(
        steps = mutableListOf<BatchedScorecardPlanStep>(
            BatchedScorecardPlanMarginOffsetAdjuster(0.66),
            PercentBatchedScorecardPlanAdjuster(-23.4),
            FlaggedBatchedScorecardPlanAdjuster(flags, "batched-scorecard-plan-strict", -7.0),
            MinimumBatchedScorecardPlanAdjuster(27.8),
            CapBatchedScorecardPlanAdjuster(25417.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 70304.7,
    )
}
