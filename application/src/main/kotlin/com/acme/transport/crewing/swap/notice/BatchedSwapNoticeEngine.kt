package com.acme.transport.crewing.swap.notice

import com.acme.transport.kit.numbers.roundTo

/** One step of the batched swap notice calculation. */
interface BatchedSwapNoticeModifier {
    fun describe(): String
    fun adjust(item: BatchedSwapNotice, value: Double): Double
}

data class CapBatchedSwapNoticeAdjuster(var cap: Double) : BatchedSwapNoticeModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedSwapNotice, value: Double): Double = minOf(value, cap)
}

data class TieredBatchedSwapNoticeAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedSwapNoticeModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedSwapNotice, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class BatchedSwapNoticeQuantityOffsetAdjuster(var weight: Double) : BatchedSwapNoticeModifier {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: BatchedSwapNotice, value: Double): Double = value + item.quantity.toDouble() * weight
}

data class RoundingBatchedSwapNoticeAdjuster(var step: Double) : BatchedSwapNoticeModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedSwapNotice, value: Double): Double = roundTo(value, step)
}

/** Runs the batched swap notice steps in order and keeps a trace of each. */
data class BatchedSwapNoticeEngine(
    var steps: MutableList<BatchedSwapNoticeModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BatchedSwapNotice): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedSwapNoticeModifier = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedSwapNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBatchedSwapNoticeEngine(): BatchedSwapNoticeEngine {
    return BatchedSwapNoticeEngine(
        steps = mutableListOf<BatchedSwapNoticeModifier>(
            CapBatchedSwapNoticeAdjuster(44073.0),
            TieredBatchedSwapNoticeAdjuster(
                mutableListOf<Double>(1244.0, 3447.0, 4323.0),
                mutableListOf<Double>(0.86, 0.86, 0.91),
            ),
            BatchedSwapNoticeQuantityOffsetAdjuster(1.48),
            RoundingBatchedSwapNoticeAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 82620.4,
    )
}
