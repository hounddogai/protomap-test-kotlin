package com.acme.transport.emissions.routeoffset.window

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the batched route offset assignment calculation. */
interface BatchedRouteOffsetAssignmentAdjuster {
    fun describe(): String
    fun adjust(item: BatchedRouteOffsetAssignment, value: Double): Double
}

data class BatchedRouteOffsetAssignmentCapacityOffsetAdjuster(
    var weight: Double,
) : BatchedRouteOffsetAssignmentAdjuster {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: BatchedRouteOffsetAssignment, value: Double): Double {
        return value + item.capacity.toDouble() * weight
    }
}

data class TieredBatchedRouteOffsetAssignmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedRouteOffsetAssignmentAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedRouteOffsetAssignment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentBatchedRouteOffsetAssignmentAdjuster(
    var percent: Double,
) : BatchedRouteOffsetAssignmentAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedRouteOffsetAssignment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapBatchedRouteOffsetAssignmentAdjuster(var cap: Double) : BatchedRouteOffsetAssignmentAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedRouteOffsetAssignment, value: Double): Double = minOf(value, cap)
}

/** Runs the batched route offset assignment steps in order and keeps a trace of each. */
data class BatchedRouteOffsetAssignmentEngine(
    var steps: MutableList<BatchedRouteOffsetAssignmentAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: BatchedRouteOffsetAssignment): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedRouteOffsetAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultBatchedRouteOffsetAssignmentEngine(): BatchedRouteOffsetAssignmentEngine {
    return BatchedRouteOffsetAssignmentEngine(
        steps = mutableListOf<BatchedRouteOffsetAssignmentAdjuster>(
            BatchedRouteOffsetAssignmentCapacityOffsetAdjuster(0.81),
            TieredBatchedRouteOffsetAssignmentAdjuster(
                mutableListOf<Double>(306.0, 3214.0, 3418.0, 9323.0),
                mutableListOf<Double>(1.04, 0.85, 1.16, 1.18),
            ),
            PercentBatchedRouteOffsetAssignmentAdjuster(8.7),
            CapBatchedRouteOffsetAssignmentAdjuster(35415.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 43912.3,
    )
}
