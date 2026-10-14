package com.acme.transport.fleetops.inspection.threshold

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the batched inspection metric calculation. */
interface BatchedInspectionMetricAdjuster {
    fun describe(): String
    fun adjust(item: BatchedInspectionMetric, value: Double): Double
}

data class LaneBatchedInspectionMetricAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedInspectionMetricAdjuster {
    override fun describe(): String = "by lane"

    override fun adjust(item: BatchedInspectionMetric, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredBatchedInspectionMetricAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedInspectionMetricAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedInspectionMetric, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapBatchedInspectionMetricAdjuster(var cap: Double) : BatchedInspectionMetricAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedInspectionMetric, value: Double): Double = minOf(value, cap)
}

/** Runs the batched inspection metric steps in order and keeps a trace of each. */
data class BatchedInspectionMetricEngine(
    var steps: MutableList<BatchedInspectionMetricAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: BatchedInspectionMetric): Double {
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

    fun evaluateAll(items: MutableList<BatchedInspectionMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultBatchedInspectionMetricEngine(): BatchedInspectionMetricEngine {
    return BatchedInspectionMetricEngine(
        steps = mutableListOf<BatchedInspectionMetricAdjuster>(
            LaneBatchedInspectionMetricAdjuster(
                mutableMapOf<String, Double>("retail" to 1.37, "west" to 1.2, "south" to 1.34),
            ),
            TieredBatchedInspectionMetricAdjuster(
                mutableListOf<Double>(2286.0, 2463.0, 4059.0, 7773.0),
                mutableListOf<Double>(1.18, 1.06, 0.91, 0.81),
            ),
            CapBatchedInspectionMetricAdjuster(24053.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 55198.7,
    )
}
