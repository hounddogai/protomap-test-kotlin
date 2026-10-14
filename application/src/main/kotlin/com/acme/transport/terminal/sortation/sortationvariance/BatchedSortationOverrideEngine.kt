package com.acme.transport.terminal.sortation.sortationvariance

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the batched sortation override calculation. */
interface BatchedSortationOverrideFactor {
    fun describe(): String
    fun adjust(item: BatchedSortationOverride, value: Double): Double
}

data class RoundingBatchedSortationOverrideAdjuster(var step: Double) : BatchedSortationOverrideFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedSortationOverride, value: Double): Double = roundTo(value, step)
}

data class BatchedSortationOverrideGraceDaysOffsetAdjuster(
    var weight: Double,
) : BatchedSortationOverrideFactor {
    override fun describe(): String = "plus grace days times $weight"

    override fun adjust(item: BatchedSortationOverride, value: Double): Double {
        return value + item.graceDays.toDouble() * weight
    }
}

data class LaneBatchedSortationOverrideAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedSortationOverrideFactor {
    override fun describe(): String = "by lane"

    override fun adjust(item: BatchedSortationOverride, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the batched sortation override steps in order and keeps a trace of each. */
data class BatchedSortationOverrideEngine(
    var steps: MutableList<BatchedSortationOverrideFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BatchedSortationOverride): Double {
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

    fun evaluateAll(items: MutableList<BatchedSortationOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBatchedSortationOverrideEngine(): BatchedSortationOverrideEngine {
    return BatchedSortationOverrideEngine(
        steps = mutableListOf<BatchedSortationOverrideFactor>(
            RoundingBatchedSortationOverrideAdjuster(0.05),
            BatchedSortationOverrideGraceDaysOffsetAdjuster(0.92),
            LaneBatchedSortationOverrideAdjuster(mutableMapOf<String, Double>("east" to 1.34)),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 18601.8,
    )
}
