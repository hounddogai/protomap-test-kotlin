package com.acme.transport.linehaul.capacity.agreement

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the batched capacity metric calculation. */
interface BatchedCapacityMetricAdjuster {
    fun describe(): String
    fun adjust(item: BatchedCapacityMetric, value: Double): Double
}

data class CapBatchedCapacityMetricAdjuster(var cap: Double) : BatchedCapacityMetricAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedCapacityMetric, value: Double): Double = minOf(value, cap)
}

data class RoundingBatchedCapacityMetricAdjuster(var step: Double) : BatchedCapacityMetricAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedCapacityMetric, value: Double): Double = roundTo(value, step)
}

data class UnitBatchedCapacityMetricAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedCapacityMetricAdjuster {
    override fun describe(): String = "by unit"

    override fun adjust(item: BatchedCapacityMetric, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumBatchedCapacityMetricAdjuster(var minimum: Double) : BatchedCapacityMetricAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedCapacityMetric, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentBatchedCapacityMetricAdjuster(var percent: Double) : BatchedCapacityMetricAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedCapacityMetric, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the batched capacity metric steps in order and keeps a trace of each. */
data class BatchedCapacityMetricEngine(
    var steps: MutableList<BatchedCapacityMetricAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: BatchedCapacityMetric): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedCapacityMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultBatchedCapacityMetricEngine(): BatchedCapacityMetricEngine {
    return BatchedCapacityMetricEngine(
        steps = mutableListOf<BatchedCapacityMetricAdjuster>(
            CapBatchedCapacityMetricAdjuster(8614.0),
            RoundingBatchedCapacityMetricAdjuster(5.0),
            UnitBatchedCapacityMetricAdjuster(mutableMapOf<String, Double>("south" to 1.11)),
            MinimumBatchedCapacityMetricAdjuster(5.6),
            PercentBatchedCapacityMetricAdjuster(-0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 95675.8,
    )
}
