package com.acme.transport.linehaul.trailer.traileroverride

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the batched trailer override calculation. */
interface BatchedTrailerOverrideAdjuster {
    fun describe(): String
    fun adjust(item: BatchedTrailerOverride, value: Double): Double
}

data class MinimumBatchedTrailerOverrideAdjuster(var minimum: Double) : BatchedTrailerOverrideAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedTrailerOverride, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentBatchedTrailerOverrideAdjuster(var percent: Double) : BatchedTrailerOverrideAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedTrailerOverride, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingBatchedTrailerOverrideAdjuster(var step: Double) : BatchedTrailerOverrideAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedTrailerOverride, value: Double): Double = roundTo(value, step)
}

data class BatchedTrailerOverrideDiscountOffsetAdjuster(var weight: Double) : BatchedTrailerOverrideAdjuster {
    override fun describe(): String = "plus discount times $weight"

    override fun adjust(item: BatchedTrailerOverride, value: Double): Double = value + item.discount * weight
}

/** Runs the batched trailer override steps in order and keeps a trace of each. */
data class BatchedTrailerOverrideEngine(
    var steps: MutableList<BatchedTrailerOverrideAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: BatchedTrailerOverride): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedTrailerOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultBatchedTrailerOverrideEngine(): BatchedTrailerOverrideEngine {
    return BatchedTrailerOverrideEngine(
        steps = mutableListOf<BatchedTrailerOverrideAdjuster>(
            MinimumBatchedTrailerOverrideAdjuster(21.8),
            PercentBatchedTrailerOverrideAdjuster(-7.1),
            RoundingBatchedTrailerOverrideAdjuster(0.5),
            BatchedTrailerOverrideDiscountOffsetAdjuster(0.59),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 74241.8,
    )
}
