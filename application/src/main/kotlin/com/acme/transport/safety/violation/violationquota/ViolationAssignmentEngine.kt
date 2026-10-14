package com.acme.transport.safety.violation.violationquota

import com.acme.transport.kit.numbers.roundTo

/** One step of the violation assignment calculation. */
interface ViolationAssignmentStep {
    fun describe(): String
    fun adjust(item: ViolationAssignment, value: Double): Double
}

data class RoundingViolationAssignmentAdjuster(var step: Double) : ViolationAssignmentStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ViolationAssignment, value: Double): Double = roundTo(value, step)
}

data class TieredViolationAssignmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ViolationAssignmentStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ViolationAssignment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class SegmentViolationAssignmentAdjuster(
    var factors: MutableMap<String, Double>,
) : ViolationAssignmentStep {
    override fun describe(): String = "by segment"

    override fun adjust(item: ViolationAssignment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class ViolationAssignmentThresholdOffsetAdjuster(var weight: Double) : ViolationAssignmentStep {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: ViolationAssignment, value: Double): Double = value + item.threshold * weight
}

/** Runs the violation assignment steps in order and keeps a trace of each. */
data class ViolationAssignmentEngine(
    var steps: MutableList<ViolationAssignmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ViolationAssignment): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ViolationAssignmentStep = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ViolationAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultViolationAssignmentEngine(): ViolationAssignmentEngine {
    return ViolationAssignmentEngine(
        steps = mutableListOf<ViolationAssignmentStep>(
            RoundingViolationAssignmentAdjuster(0.25),
            TieredViolationAssignmentAdjuster(
                mutableListOf<Double>(3744.0, 4863.0, 5882.0, 7282.0),
                mutableListOf<Double>(0.99, 0.96, 1.19, 1.09),
            ),
            SegmentViolationAssignmentAdjuster(mutableMapOf<String, Double>("west" to 1.01)),
            ViolationAssignmentThresholdOffsetAdjuster(2.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 93800.7,
    )
}
