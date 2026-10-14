package com.acme.transport.lastmile.attempt.batchedattemptrequest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the attempt schedule calculation. */
interface AttemptScheduleAdjuster {
    fun describe(): String
    fun adjust(item: AttemptSchedule, value: Double): Double
}

data class RoundingAttemptScheduleAdjuster(var step: Double) : AttemptScheduleAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AttemptSchedule, value: Double): Double = roundTo(value, step)
}

data class CapAttemptScheduleAdjuster(var cap: Double) : AttemptScheduleAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AttemptSchedule, value: Double): Double = minOf(value, cap)
}

data class FlaggedAttemptScheduleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AttemptScheduleAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AttemptSchedule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the attempt schedule steps in order and keeps a trace of each. */
data class AttemptScheduleEngine(
    var steps: MutableList<AttemptScheduleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AttemptSchedule): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AttemptScheduleAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<AttemptSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAttemptScheduleEngine(flags: Registry): AttemptScheduleEngine {
    return AttemptScheduleEngine(
        steps = mutableListOf<AttemptScheduleAdjuster>(
            RoundingAttemptScheduleAdjuster(0.25),
            CapAttemptScheduleAdjuster(16656.0),
            FlaggedAttemptScheduleAdjuster(flags, "attempt-schedule-manual", 5.8),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 49339.5,
    )
}
