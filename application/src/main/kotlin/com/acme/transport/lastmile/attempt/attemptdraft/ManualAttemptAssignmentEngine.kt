package com.acme.transport.lastmile.attempt.attemptdraft

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the manual attempt assignment calculation. */
interface ManualAttemptAssignmentStep {
    fun describe(): String
    fun adjust(item: ManualAttemptAssignment, value: Double): Double
}

data class SegmentManualAttemptAssignmentAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualAttemptAssignmentStep {
    override fun describe(): String = "by segment"

    override fun adjust(item: ManualAttemptAssignment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingManualAttemptAssignmentAdjuster(var step: Double) : ManualAttemptAssignmentStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualAttemptAssignment, value: Double): Double = roundTo(value, step)
}

data class PercentManualAttemptAssignmentAdjuster(var percent: Double) : ManualAttemptAssignmentStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualAttemptAssignment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedManualAttemptAssignmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualAttemptAssignmentStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualAttemptAssignment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapManualAttemptAssignmentAdjuster(var cap: Double) : ManualAttemptAssignmentStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualAttemptAssignment, value: Double): Double = minOf(value, cap)
}

data class MinimumManualAttemptAssignmentAdjuster(var minimum: Double) : ManualAttemptAssignmentStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualAttemptAssignment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the manual attempt assignment steps in order and keeps a trace of each. */
data class ManualAttemptAssignmentEngine(
    var steps: MutableList<ManualAttemptAssignmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ManualAttemptAssignment): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ManualAttemptAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultManualAttemptAssignmentEngine(flags: Registry): ManualAttemptAssignmentEngine {
    return ManualAttemptAssignmentEngine(
        steps = mutableListOf<ManualAttemptAssignmentStep>(
            SegmentManualAttemptAssignmentAdjuster(
                mutableMapOf<String, Double>("online" to 0.9, "central" to 1.06),
            ),
            RoundingManualAttemptAssignmentAdjuster(0.5),
            PercentManualAttemptAssignmentAdjuster(-3.4),
            FlaggedManualAttemptAssignmentAdjuster(flags, "manual-attempt-assignment-seasonal", 4.6),
            CapManualAttemptAssignmentAdjuster(34049.0),
            MinimumManualAttemptAssignmentAdjuster(35.7),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 98699.5,
    )
}
