package com.acme.transport.crewing.swap.estimate

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the batched swap estimate calculation. */
interface BatchedSwapEstimateModifier {
    fun describe(): String
    fun adjust(item: BatchedSwapEstimate, value: Double): Double
}

data class TieredBatchedSwapEstimateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedSwapEstimateModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedSwapEstimate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingBatchedSwapEstimateAdjuster(var step: Double) : BatchedSwapEstimateModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedSwapEstimate, value: Double): Double = roundTo(value, step)
}

data class PercentBatchedSwapEstimateAdjuster(var percent: Double) : BatchedSwapEstimateModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedSwapEstimate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedBatchedSwapEstimateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedSwapEstimateModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedSwapEstimate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumBatchedSwapEstimateAdjuster(var minimum: Double) : BatchedSwapEstimateModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedSwapEstimate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class BatchedSwapEstimateCostOffsetAdjuster(var weight: Double) : BatchedSwapEstimateModifier {
    override fun describe(): String = "plus cost times $weight"

    override fun adjust(item: BatchedSwapEstimate, value: Double): Double = value + item.cost * weight
}

/** Runs the batched swap estimate steps in order and keeps a trace of each. */
data class BatchedSwapEstimateEngine(
    var steps: MutableList<BatchedSwapEstimateModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: BatchedSwapEstimate): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedSwapEstimateModifier = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedSwapEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultBatchedSwapEstimateEngine(flags: Registry): BatchedSwapEstimateEngine {
    return BatchedSwapEstimateEngine(
        steps = mutableListOf<BatchedSwapEstimateModifier>(
            TieredBatchedSwapEstimateAdjuster(
                mutableListOf<Double>(1244.0, 6161.0, 9566.0, 9920.0),
                mutableListOf<Double>(0.82, 1.13, 1.07, 1.08),
            ),
            RoundingBatchedSwapEstimateAdjuster(0.5),
            PercentBatchedSwapEstimateAdjuster(-16.3),
            FlaggedBatchedSwapEstimateAdjuster(flags, "batched-swap-estimate-tiered", 6.0),
            MinimumBatchedSwapEstimateAdjuster(28.6),
            BatchedSwapEstimateCostOffsetAdjuster(0.74),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 8962.9,
    )
}
