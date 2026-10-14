package com.acme.transport.freightpay.dispute.adaptivedisputeassignment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the batched dispute assignment calculation. */
interface BatchedDisputeAssignmentAdjuster {
    fun describe(): String
    fun adjust(item: BatchedDisputeAssignment, value: Double): Double
}

data class TieredBatchedDisputeAssignmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedDisputeAssignmentAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedDisputeAssignment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class DepotBatchedDisputeAssignmentAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedDisputeAssignmentAdjuster {
    override fun describe(): String = "by depot"

    override fun adjust(item: BatchedDisputeAssignment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumBatchedDisputeAssignmentAdjuster(var minimum: Double) : BatchedDisputeAssignmentAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedDisputeAssignment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedBatchedDisputeAssignmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedDisputeAssignmentAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedDisputeAssignment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapBatchedDisputeAssignmentAdjuster(var cap: Double) : BatchedDisputeAssignmentAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedDisputeAssignment, value: Double): Double = minOf(value, cap)
}

/** Runs the batched dispute assignment steps in order and keeps a trace of each. */
data class BatchedDisputeAssignmentEngine(
    var steps: MutableList<BatchedDisputeAssignmentAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BatchedDisputeAssignment): Double {
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

    fun evaluateAll(items: MutableList<BatchedDisputeAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBatchedDisputeAssignmentEngine(flags: Registry): BatchedDisputeAssignmentEngine {
    return BatchedDisputeAssignmentEngine(
        steps = mutableListOf<BatchedDisputeAssignmentAdjuster>(
            TieredBatchedDisputeAssignmentAdjuster(
                mutableListOf<Double>(4055.0, 9446.0),
                mutableListOf<Double>(0.98, 0.93),
            ),
            DepotBatchedDisputeAssignmentAdjuster(
                mutableMapOf<String, Double>("north" to 1.04, "retail" to 1.35),
            ),
            MinimumBatchedDisputeAssignmentAdjuster(37.2),
            FlaggedBatchedDisputeAssignmentAdjuster(flags, "batched-dispute-assignment-fallback", -6.6),
            CapBatchedDisputeAssignmentAdjuster(25453.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 91407.8,
    )
}
