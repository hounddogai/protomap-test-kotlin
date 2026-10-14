package com.acme.transport.safety.coaching.assignment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the coaching assignment calculation. */
interface CoachingAssignmentAdjuster {
    fun describe(): String
    fun adjust(item: CoachingAssignment, value: Double): Double
}

data class FlaggedCoachingAssignmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CoachingAssignmentAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CoachingAssignment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingCoachingAssignmentAdjuster(var step: Double) : CoachingAssignmentAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CoachingAssignment, value: Double): Double = roundTo(value, step)
}

data class MinimumCoachingAssignmentAdjuster(var minimum: Double) : CoachingAssignmentAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CoachingAssignment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the coaching assignment steps in order and keeps a trace of each. */
data class CoachingAssignmentEngine(
    var steps: MutableList<CoachingAssignmentAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CoachingAssignment): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CoachingAssignmentAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<CoachingAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCoachingAssignmentEngine(flags: Registry): CoachingAssignmentEngine {
    return CoachingAssignmentEngine(
        steps = mutableListOf<CoachingAssignmentAdjuster>(
            FlaggedCoachingAssignmentAdjuster(flags, "coaching-assignment-scheduled", 0.4),
            RoundingCoachingAssignmentAdjuster(1.0),
            MinimumCoachingAssignmentAdjuster(10.6),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 56997.8,
    )
}
