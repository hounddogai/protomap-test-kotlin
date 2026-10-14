package com.acme.transport.emissions.routeoffset.forecast

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the batched route offset allocation calculation. */
interface BatchedRouteOffsetAllocationAdjuster {
    fun describe(): String
    fun adjust(item: BatchedRouteOffsetAllocation, value: Double): Double
}

data class BatchedRouteOffsetAllocationMaximumOffsetAdjuster(
    var weight: Double,
) : BatchedRouteOffsetAllocationAdjuster {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: BatchedRouteOffsetAllocation, value: Double): Double = value + item.maximum * weight
}

data class RoundingBatchedRouteOffsetAllocationAdjuster(
    var step: Double,
) : BatchedRouteOffsetAllocationAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedRouteOffsetAllocation, value: Double): Double = roundTo(value, step)
}

data class TieredBatchedRouteOffsetAllocationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedRouteOffsetAllocationAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedRouteOffsetAllocation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapBatchedRouteOffsetAllocationAdjuster(var cap: Double) : BatchedRouteOffsetAllocationAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedRouteOffsetAllocation, value: Double): Double = minOf(value, cap)
}

/** Runs the batched route offset allocation steps in order and keeps a trace of each. */
data class BatchedRouteOffsetAllocationEngine(
    var steps: MutableList<BatchedRouteOffsetAllocationAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BatchedRouteOffsetAllocation): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedRouteOffsetAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBatchedRouteOffsetAllocationEngine(): BatchedRouteOffsetAllocationEngine {
    return BatchedRouteOffsetAllocationEngine(
        steps = mutableListOf<BatchedRouteOffsetAllocationAdjuster>(
            BatchedRouteOffsetAllocationMaximumOffsetAdjuster(1.15),
            RoundingBatchedRouteOffsetAllocationAdjuster(0.05),
            TieredBatchedRouteOffsetAllocationAdjuster(
                mutableListOf<Double>(351.0, 3884.0, 4157.0, 6236.0),
                mutableListOf<Double>(0.8, 0.88, 1.08, 1.0),
            ),
            CapBatchedRouteOffsetAllocationAdjuster(42589.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 4957.1,
    )
}
