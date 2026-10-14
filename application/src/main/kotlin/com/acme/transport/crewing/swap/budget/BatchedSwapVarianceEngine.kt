package com.acme.transport.crewing.swap.budget

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the batched swap variance calculation. */
interface BatchedSwapVarianceFactor {
    fun describe(): String
    fun adjust(item: BatchedSwapVariance, value: Double): Double
}

data class FlaggedBatchedSwapVarianceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedSwapVarianceFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedSwapVariance, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapBatchedSwapVarianceAdjuster(var cap: Double) : BatchedSwapVarianceFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedSwapVariance, value: Double): Double = minOf(value, cap)
}

data class RoundingBatchedSwapVarianceAdjuster(var step: Double) : BatchedSwapVarianceFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedSwapVariance, value: Double): Double = roundTo(value, step)
}

data class BatchedSwapVarianceStopCountOffsetAdjuster(var weight: Double) : BatchedSwapVarianceFactor {
    override fun describe(): String = "plus stop count times $weight"

    override fun adjust(item: BatchedSwapVariance, value: Double): Double = value + item.stopCount.toDouble() * weight
}

data class PercentBatchedSwapVarianceAdjuster(var percent: Double) : BatchedSwapVarianceFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedSwapVariance, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the batched swap variance steps in order and keeps a trace of each. */
data class BatchedSwapVarianceEngine(
    var steps: MutableList<BatchedSwapVarianceFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: BatchedSwapVariance): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedSwapVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultBatchedSwapVarianceEngine(flags: Registry): BatchedSwapVarianceEngine {
    return BatchedSwapVarianceEngine(
        steps = mutableListOf<BatchedSwapVarianceFactor>(
            FlaggedBatchedSwapVarianceAdjuster(flags, "batched-swap-variance-lenient", -2.6),
            CapBatchedSwapVarianceAdjuster(45687.0),
            RoundingBatchedSwapVarianceAdjuster(1.0),
            BatchedSwapVarianceStopCountOffsetAdjuster(1.12),
            PercentBatchedSwapVarianceAdjuster(-7.3),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 54790.3,
    )
}
