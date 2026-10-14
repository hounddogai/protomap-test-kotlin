package com.acme.transport.network.node.assignment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the batched node assignment calculation. */
interface BatchedNodeAssignmentModifier {
    fun describe(): String
    fun adjust(item: BatchedNodeAssignment, value: Double): Double
}

data class RoundingBatchedNodeAssignmentAdjuster(var step: Double) : BatchedNodeAssignmentModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedNodeAssignment, value: Double): Double = roundTo(value, step)
}

data class FlaggedBatchedNodeAssignmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedNodeAssignmentModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedNodeAssignment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TitleBatchedNodeAssignmentAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedNodeAssignmentModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: BatchedNodeAssignment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentBatchedNodeAssignmentAdjuster(var percent: Double) : BatchedNodeAssignmentModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedNodeAssignment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class BatchedNodeAssignmentDistanceKmOffsetAdjuster(var weight: Double) : BatchedNodeAssignmentModifier {
    override fun describe(): String = "plus distance km times $weight"

    override fun adjust(item: BatchedNodeAssignment, value: Double): Double = value + item.distanceKm * weight
}

data class MinimumBatchedNodeAssignmentAdjuster(var minimum: Double) : BatchedNodeAssignmentModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedNodeAssignment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the batched node assignment steps in order and keeps a trace of each. */
data class BatchedNodeAssignmentEngine(
    var steps: MutableList<BatchedNodeAssignmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: BatchedNodeAssignment): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedNodeAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultBatchedNodeAssignmentEngine(flags: Registry): BatchedNodeAssignmentEngine {
    return BatchedNodeAssignmentEngine(
        steps = mutableListOf<BatchedNodeAssignmentModifier>(
            RoundingBatchedNodeAssignmentAdjuster(0.25),
            FlaggedBatchedNodeAssignmentAdjuster(flags, "batched-node-assignment-legacy", -2.5),
            TitleBatchedNodeAssignmentAdjuster(
                mutableMapOf<String, Double>("online" to 1.14, "central" to 0.73),
            ),
            PercentBatchedNodeAssignmentAdjuster(14.1),
            BatchedNodeAssignmentDistanceKmOffsetAdjuster(0.86),
            MinimumBatchedNodeAssignmentAdjuster(20.5),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 90500.8,
    )
}
