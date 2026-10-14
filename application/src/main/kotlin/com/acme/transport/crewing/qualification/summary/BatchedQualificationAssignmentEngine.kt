package com.acme.transport.crewing.qualification.summary

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the batched qualification assignment calculation. */
interface BatchedQualificationAssignmentAdjuster {
    fun describe(): String
    fun adjust(item: BatchedQualificationAssignment, value: Double): Double
}

data class NameBatchedQualificationAssignmentAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedQualificationAssignmentAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: BatchedQualificationAssignment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingBatchedQualificationAssignmentAdjuster(
    var step: Double,
) : BatchedQualificationAssignmentAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedQualificationAssignment, value: Double): Double = roundTo(value, step)
}

data class MinimumBatchedQualificationAssignmentAdjuster(
    var minimum: Double,
) : BatchedQualificationAssignmentAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedQualificationAssignment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentBatchedQualificationAssignmentAdjuster(
    var percent: Double,
) : BatchedQualificationAssignmentAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedQualificationAssignment, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the batched qualification assignment steps in order and keeps a trace of each. */
data class BatchedQualificationAssignmentEngine(
    var steps: MutableList<BatchedQualificationAssignmentAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BatchedQualificationAssignment): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedQualificationAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBatchedQualificationAssignmentEngine(): BatchedQualificationAssignmentEngine {
    return BatchedQualificationAssignmentEngine(
        steps = mutableListOf<BatchedQualificationAssignmentAdjuster>(
            NameBatchedQualificationAssignmentAdjuster(
                mutableMapOf<String, Double>("west" to 0.79, "online" to 0.93, "north" to 1.12),
            ),
            RoundingBatchedQualificationAssignmentAdjuster(0.05),
            MinimumBatchedQualificationAssignmentAdjuster(30.5),
            PercentBatchedQualificationAssignmentAdjuster(-8.4),
        ),
        trace = mutableListOf<String>(),
        floor = 5.0,
        ceiling = 55597.6,
    )
}
