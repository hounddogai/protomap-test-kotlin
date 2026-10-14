package com.acme.transport.fleetops.serviceplan.planpreference

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the batched service plan summary calculation. */
interface BatchedServicePlanSummaryStep {
    fun describe(): String
    fun adjust(item: BatchedServicePlanSummary, value: Double): Double
}

data class TieredBatchedServicePlanSummaryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedServicePlanSummaryStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedServicePlanSummary, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumBatchedServicePlanSummaryAdjuster(var minimum: Double) : BatchedServicePlanSummaryStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedServicePlanSummary, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapBatchedServicePlanSummaryAdjuster(var cap: Double) : BatchedServicePlanSummaryStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedServicePlanSummary, value: Double): Double = minOf(value, cap)
}

/** Runs the batched service plan summary steps in order and keeps a trace of each. */
data class BatchedServicePlanSummaryEngine(
    var steps: MutableList<BatchedServicePlanSummaryStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: BatchedServicePlanSummary): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedServicePlanSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultBatchedServicePlanSummaryEngine(): BatchedServicePlanSummaryEngine {
    return BatchedServicePlanSummaryEngine(
        steps = mutableListOf<BatchedServicePlanSummaryStep>(
            TieredBatchedServicePlanSummaryAdjuster(
                mutableListOf<Double>(1463.0, 3408.0, 6358.0, 6625.0),
                mutableListOf<Double>(0.86, 1.11, 0.99, 1.16),
            ),
            MinimumBatchedServicePlanSummaryAdjuster(21.2),
            CapBatchedServicePlanSummaryAdjuster(15052.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 59492.6,
    )
}
