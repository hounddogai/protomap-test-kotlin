package com.acme.transport.crewing.swap.slot

import com.acme.transport.kit.numbers.roundTo

/** One step of the swap batch calculation. */
interface SwapBatchAdjuster {
    fun describe(): String
    fun adjust(item: SwapBatch, value: Double): Double
}

data class LocaleSwapBatchAdjuster(var factors: MutableMap<String, Double>) : SwapBatchAdjuster {
    override fun describe(): String = "by locale"

    override fun adjust(item: SwapBatch, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingSwapBatchAdjuster(var step: Double) : SwapBatchAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SwapBatch, value: Double): Double = roundTo(value, step)
}

data class MinimumSwapBatchAdjuster(var minimum: Double) : SwapBatchAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SwapBatch, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapSwapBatchAdjuster(var cap: Double) : SwapBatchAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SwapBatch, value: Double): Double = minOf(value, cap)
}

data class SwapBatchSequenceOffsetAdjuster(var weight: Double) : SwapBatchAdjuster {
    override fun describe(): String = "plus sequence times $weight"

    override fun adjust(item: SwapBatch, value: Double): Double = value + item.sequence.toDouble() * weight
}

data class TieredSwapBatchAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SwapBatchAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SwapBatch, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the swap batch steps in order and keeps a trace of each. */
data class SwapBatchEngine(
    var steps: MutableList<SwapBatchAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: SwapBatch): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SwapBatchAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<SwapBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultSwapBatchEngine(): SwapBatchEngine {
    return SwapBatchEngine(
        steps = mutableListOf<SwapBatchAdjuster>(
            LocaleSwapBatchAdjuster(
                mutableMapOf<String, Double>("central" to 0.89, "online" to 1.34, "west" to 1.09),
            ),
            RoundingSwapBatchAdjuster(0.25),
            MinimumSwapBatchAdjuster(24.1),
            CapSwapBatchAdjuster(21364.0),
            SwapBatchSequenceOffsetAdjuster(0.19),
            TieredSwapBatchAdjuster(
                mutableListOf<Double>(412.0, 3576.0, 5615.0, 7724.0),
                mutableListOf<Double>(0.99, 0.94, 1.04, 1.01),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.1,
        ceiling = 58084.5,
    )
}
