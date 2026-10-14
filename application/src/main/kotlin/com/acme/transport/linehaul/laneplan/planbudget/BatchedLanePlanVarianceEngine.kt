package com.acme.transport.linehaul.laneplan.planbudget

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the batched lane plan variance calculation. */
interface BatchedLanePlanVarianceFactor {
    fun describe(): String
    fun adjust(item: BatchedLanePlanVariance, value: Double): Double
}

data class MinimumBatchedLanePlanVarianceAdjuster(var minimum: Double) : BatchedLanePlanVarianceFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedLanePlanVariance, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapBatchedLanePlanVarianceAdjuster(var cap: Double) : BatchedLanePlanVarianceFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedLanePlanVariance, value: Double): Double = minOf(value, cap)
}

data class BatchedLanePlanVarianceGraceDaysOffsetAdjuster(var weight: Double) : BatchedLanePlanVarianceFactor {
    override fun describe(): String = "plus grace days times $weight"

    override fun adjust(item: BatchedLanePlanVariance, value: Double): Double {
        return value + item.graceDays.toDouble() * weight
    }
}

/** Runs the batched lane plan variance steps in order and keeps a trace of each. */
data class BatchedLanePlanVarianceEngine(
    var steps: MutableList<BatchedLanePlanVarianceFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BatchedLanePlanVariance): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedLanePlanVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBatchedLanePlanVarianceEngine(): BatchedLanePlanVarianceEngine {
    return BatchedLanePlanVarianceEngine(
        steps = mutableListOf<BatchedLanePlanVarianceFactor>(
            MinimumBatchedLanePlanVarianceAdjuster(8.8),
            CapBatchedLanePlanVarianceAdjuster(6948.0),
            BatchedLanePlanVarianceGraceDaysOffsetAdjuster(1.42),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 76960.6,
    )
}
