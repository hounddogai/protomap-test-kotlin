package com.acme.transport.network.servicearea.cycle

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the batched service area exception calculation. */
interface BatchedServiceAreaExceptionStep {
    fun describe(): String
    fun adjust(item: BatchedServiceAreaException, value: Double): Double
}

data class MinimumBatchedServiceAreaExceptionAdjuster(var minimum: Double) : BatchedServiceAreaExceptionStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedServiceAreaException, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentBatchedServiceAreaExceptionAdjuster(var percent: Double) : BatchedServiceAreaExceptionStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedServiceAreaException, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class SegmentBatchedServiceAreaExceptionAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedServiceAreaExceptionStep {
    override fun describe(): String = "by segment"

    override fun adjust(item: BatchedServiceAreaException, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the batched service area exception steps in order and keeps a trace of each. */
data class BatchedServiceAreaExceptionEngine(
    var steps: MutableList<BatchedServiceAreaExceptionStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: BatchedServiceAreaException): Double {
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

    fun evaluateAll(items: MutableList<BatchedServiceAreaException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultBatchedServiceAreaExceptionEngine(): BatchedServiceAreaExceptionEngine {
    return BatchedServiceAreaExceptionEngine(
        steps = mutableListOf<BatchedServiceAreaExceptionStep>(
            MinimumBatchedServiceAreaExceptionAdjuster(34.7),
            PercentBatchedServiceAreaExceptionAdjuster(2.3),
            SegmentBatchedServiceAreaExceptionAdjuster(
                mutableMapOf<String, Double>("central" to 0.88, "online" to 0.84, "west" to 1.25),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 86404.1,
    )
}
