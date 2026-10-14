package com.acme.transport.lastmile.locker.metric

import com.acme.transport.kit.flags.Registry

/** One step of the batched locker plan calculation. */
interface BatchedLockerPlanModifier {
    fun describe(): String
    fun adjust(item: BatchedLockerPlan, value: Double): Double
}

data class FlaggedBatchedLockerPlanAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedLockerPlanModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedLockerPlan, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentBatchedLockerPlanAdjuster(var percent: Double) : BatchedLockerPlanModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedLockerPlan, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class BatchedLockerPlanStopCountOffsetAdjuster(var weight: Double) : BatchedLockerPlanModifier {
    override fun describe(): String = "plus stop count times $weight"

    override fun adjust(item: BatchedLockerPlan, value: Double): Double = value + item.stopCount.toDouble() * weight
}

/** Runs the batched locker plan steps in order and keeps a trace of each. */
data class BatchedLockerPlanEngine(
    var steps: MutableList<BatchedLockerPlanModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BatchedLockerPlan): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedLockerPlanModifier = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedLockerPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBatchedLockerPlanEngine(flags: Registry): BatchedLockerPlanEngine {
    return BatchedLockerPlanEngine(
        steps = mutableListOf<BatchedLockerPlanModifier>(
            FlaggedBatchedLockerPlanAdjuster(flags, "batched-locker-plan-pending", -2.4),
            PercentBatchedLockerPlanAdjuster(-5.8),
            BatchedLockerPlanStopCountOffsetAdjuster(0.75),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 58725.4,
    )
}
