package com.acme.transport.lastmile.redelivery.milestone

import com.acme.transport.kit.flags.Registry

/** One step of the redelivery schedule calculation. */
interface RedeliveryScheduleAdjuster {
    fun describe(): String
    fun adjust(item: RedeliverySchedule, value: Double): Double
}

data class PercentRedeliveryScheduleAdjuster(var percent: Double) : RedeliveryScheduleAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RedeliverySchedule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapRedeliveryScheduleAdjuster(var cap: Double) : RedeliveryScheduleAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RedeliverySchedule, value: Double): Double = minOf(value, cap)
}

data class FlaggedRedeliveryScheduleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RedeliveryScheduleAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RedeliverySchedule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumRedeliveryScheduleAdjuster(var minimum: Double) : RedeliveryScheduleAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RedeliverySchedule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the redelivery schedule steps in order and keeps a trace of each. */
data class RedeliveryScheduleEngine(
    var steps: MutableList<RedeliveryScheduleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RedeliverySchedule): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RedeliveryScheduleAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RedeliverySchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRedeliveryScheduleEngine(flags: Registry): RedeliveryScheduleEngine {
    return RedeliveryScheduleEngine(
        steps = mutableListOf<RedeliveryScheduleAdjuster>(
            PercentRedeliveryScheduleAdjuster(-13.5),
            CapRedeliveryScheduleAdjuster(46084.0),
            FlaggedRedeliveryScheduleAdjuster(flags, "redelivery-schedule-regional", -7.6),
            MinimumRedeliveryScheduleAdjuster(5.8),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 81771.0,
    )
}
