package com.acme.transport.crewing.roster.rosteragreement

import com.acme.transport.kit.numbers.roundTo

/** One step of the scheduled roster schedule calculation. */
interface ScheduledRosterScheduleAdjuster {
    fun describe(): String
    fun adjust(item: ScheduledRosterSchedule, value: Double): Double
}

data class RoundingScheduledRosterScheduleAdjuster(var step: Double) : ScheduledRosterScheduleAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledRosterSchedule, value: Double): Double = roundTo(value, step)
}

data class ScheduledRosterScheduleStopCountOffsetAdjuster(
    var weight: Double,
) : ScheduledRosterScheduleAdjuster {
    override fun describe(): String = "plus stop count times $weight"

    override fun adjust(item: ScheduledRosterSchedule, value: Double): Double {
        return value + item.stopCount.toDouble() * weight
    }
}

data class MinimumScheduledRosterScheduleAdjuster(var minimum: Double) : ScheduledRosterScheduleAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledRosterSchedule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the scheduled roster schedule steps in order and keeps a trace of each. */
data class ScheduledRosterScheduleEngine(
    var steps: MutableList<ScheduledRosterScheduleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ScheduledRosterSchedule): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledRosterScheduleAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledRosterSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultScheduledRosterScheduleEngine(): ScheduledRosterScheduleEngine {
    return ScheduledRosterScheduleEngine(
        steps = mutableListOf<ScheduledRosterScheduleAdjuster>(
            RoundingScheduledRosterScheduleAdjuster(0.1),
            ScheduledRosterScheduleStopCountOffsetAdjuster(1.12),
            MinimumScheduledRosterScheduleAdjuster(31.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 77571.8,
    )
}
