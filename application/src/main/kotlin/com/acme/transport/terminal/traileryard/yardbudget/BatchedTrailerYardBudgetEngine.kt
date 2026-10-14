package com.acme.transport.terminal.traileryard.yardbudget

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the batched trailer yard budget calculation. */
interface BatchedTrailerYardBudgetFactor {
    fun describe(): String
    fun adjust(item: BatchedTrailerYardBudget, value: Double): Double
}

data class RoundingBatchedTrailerYardBudgetAdjuster(var step: Double) : BatchedTrailerYardBudgetFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedTrailerYardBudget, value: Double): Double = roundTo(value, step)
}

data class NameBatchedTrailerYardBudgetAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedTrailerYardBudgetFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: BatchedTrailerYardBudget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentBatchedTrailerYardBudgetAdjuster(var percent: Double) : BatchedTrailerYardBudgetFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedTrailerYardBudget, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the batched trailer yard budget steps in order and keeps a trace of each. */
data class BatchedTrailerYardBudgetEngine(
    var steps: MutableList<BatchedTrailerYardBudgetFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: BatchedTrailerYardBudget): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedTrailerYardBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultBatchedTrailerYardBudgetEngine(): BatchedTrailerYardBudgetEngine {
    return BatchedTrailerYardBudgetEngine(
        steps = mutableListOf<BatchedTrailerYardBudgetFactor>(
            RoundingBatchedTrailerYardBudgetAdjuster(0.5),
            NameBatchedTrailerYardBudgetAdjuster(mutableMapOf<String, Double>("south" to 1.12)),
            PercentBatchedTrailerYardBudgetAdjuster(-6.5),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 75823.7,
    )
}
