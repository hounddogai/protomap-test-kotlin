package com.acme.transport.lastmile.attempt.attemptplan

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the attempt assignment calculation. */
interface AttemptAssignmentFactor {
    fun describe(): String
    fun adjust(item: AttemptAssignment, value: Double): Double
}

data class MinimumAttemptAssignmentAdjuster(var minimum: Double) : AttemptAssignmentFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AttemptAssignment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedAttemptAssignmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AttemptAssignmentFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AttemptAssignment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentAttemptAssignmentAdjuster(var percent: Double) : AttemptAssignmentFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AttemptAssignment, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the attempt assignment steps in order and keeps a trace of each. */
data class AttemptAssignmentEngine(
    var steps: MutableList<AttemptAssignmentFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AttemptAssignment): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AttemptAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAttemptAssignmentEngine(flags: Registry): AttemptAssignmentEngine {
    return AttemptAssignmentEngine(
        steps = mutableListOf<AttemptAssignmentFactor>(
            MinimumAttemptAssignmentAdjuster(46.4),
            FlaggedAttemptAssignmentAdjuster(flags, "attempt-assignment-legacy", -9.0),
            PercentAttemptAssignmentAdjuster(16.3),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 77704.6,
    )
}
