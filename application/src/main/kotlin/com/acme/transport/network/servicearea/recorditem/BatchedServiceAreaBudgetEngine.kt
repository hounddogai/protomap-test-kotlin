package com.acme.transport.network.servicearea.recorditem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the batched service area budget calculation. */
interface BatchedServiceAreaBudgetStep {
    fun describe(): String
    fun adjust(item: BatchedServiceAreaBudget, value: Double): Double
}

data class RoundingBatchedServiceAreaBudgetAdjuster(var step: Double) : BatchedServiceAreaBudgetStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedServiceAreaBudget, value: Double): Double = roundTo(value, step)
}

data class FlaggedBatchedServiceAreaBudgetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedServiceAreaBudgetStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedServiceAreaBudget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentBatchedServiceAreaBudgetAdjuster(var percent: Double) : BatchedServiceAreaBudgetStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedServiceAreaBudget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class BatchedServiceAreaBudgetOdometerOffsetAdjuster(var weight: Double) : BatchedServiceAreaBudgetStep {
    override fun describe(): String = "plus odometer times $weight"

    override fun adjust(item: BatchedServiceAreaBudget, value: Double): Double {
        return value + item.odometer.toDouble() * weight
    }
}

data class CapBatchedServiceAreaBudgetAdjuster(var cap: Double) : BatchedServiceAreaBudgetStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedServiceAreaBudget, value: Double): Double = minOf(value, cap)
}

/** Runs the batched service area budget steps in order and keeps a trace of each. */
data class BatchedServiceAreaBudgetEngine(
    var steps: MutableList<BatchedServiceAreaBudgetStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BatchedServiceAreaBudget): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedServiceAreaBudgetStep = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedServiceAreaBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBatchedServiceAreaBudgetEngine(flags: Registry): BatchedServiceAreaBudgetEngine {
    return BatchedServiceAreaBudgetEngine(
        steps = mutableListOf<BatchedServiceAreaBudgetStep>(
            RoundingBatchedServiceAreaBudgetAdjuster(0.5),
            FlaggedBatchedServiceAreaBudgetAdjuster(flags, "batched-service-area-budget-scheduled", 8.6),
            PercentBatchedServiceAreaBudgetAdjuster(13.8),
            BatchedServiceAreaBudgetOdometerOffsetAdjuster(0.94),
            CapBatchedServiceAreaBudgetAdjuster(34445.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 74347.9,
    )
}
