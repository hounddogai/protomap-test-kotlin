package com.acme.transport.crewing.restbreak.policy

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the batched rest break budget calculation. */
interface BatchedRestBreakBudgetStep {
    fun describe(): String
    fun adjust(item: BatchedRestBreakBudget, value: Double): Double
}

data class FlaggedBatchedRestBreakBudgetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedRestBreakBudgetStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedRestBreakBudget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredBatchedRestBreakBudgetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedRestBreakBudgetStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedRestBreakBudget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class BatchedRestBreakBudgetRatioOffsetAdjuster(var weight: Double) : BatchedRestBreakBudgetStep {
    override fun describe(): String = "plus ratio times $weight"

    override fun adjust(item: BatchedRestBreakBudget, value: Double): Double = value + item.ratio * weight
}

data class CapBatchedRestBreakBudgetAdjuster(var cap: Double) : BatchedRestBreakBudgetStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedRestBreakBudget, value: Double): Double = minOf(value, cap)
}

data class PercentBatchedRestBreakBudgetAdjuster(var percent: Double) : BatchedRestBreakBudgetStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedRestBreakBudget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingBatchedRestBreakBudgetAdjuster(var step: Double) : BatchedRestBreakBudgetStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedRestBreakBudget, value: Double): Double = roundTo(value, step)
}

/** Runs the batched rest break budget steps in order and keeps a trace of each. */
data class BatchedRestBreakBudgetEngine(
    var steps: MutableList<BatchedRestBreakBudgetStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: BatchedRestBreakBudget): Double {
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

    fun evaluateAll(items: MutableList<BatchedRestBreakBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultBatchedRestBreakBudgetEngine(flags: Registry): BatchedRestBreakBudgetEngine {
    return BatchedRestBreakBudgetEngine(
        steps = mutableListOf<BatchedRestBreakBudgetStep>(
            FlaggedBatchedRestBreakBudgetAdjuster(flags, "batched-rest-break-budget-tiered", -2.6),
            TieredBatchedRestBreakBudgetAdjuster(
                mutableListOf<Double>(1847.0, 5216.0),
                mutableListOf<Double>(0.81, 0.88),
            ),
            BatchedRestBreakBudgetRatioOffsetAdjuster(0.58),
            CapBatchedRestBreakBudgetAdjuster(43353.0),
            PercentBatchedRestBreakBudgetAdjuster(-23.3),
            RoundingBatchedRestBreakBudgetAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 74624.9,
    )
}
