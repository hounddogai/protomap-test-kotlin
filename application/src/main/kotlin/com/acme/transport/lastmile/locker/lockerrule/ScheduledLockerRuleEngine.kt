package com.acme.transport.lastmile.locker.lockerrule

import com.acme.transport.kit.flags.Registry

/** One step of the scheduled locker rule calculation. */
interface ScheduledLockerRuleAdjuster {
    fun describe(): String
    fun adjust(item: ScheduledLockerRule, value: Double): Double
}

data class FlaggedScheduledLockerRuleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledLockerRuleAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledLockerRule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentScheduledLockerRuleAdjuster(var percent: Double) : ScheduledLockerRuleAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledLockerRule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumScheduledLockerRuleAdjuster(var minimum: Double) : ScheduledLockerRuleAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledLockerRule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the scheduled locker rule steps in order and keeps a trace of each. */
data class ScheduledLockerRuleEngine(
    var steps: MutableList<ScheduledLockerRuleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ScheduledLockerRule): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledLockerRuleAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledLockerRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultScheduledLockerRuleEngine(flags: Registry): ScheduledLockerRuleEngine {
    return ScheduledLockerRuleEngine(
        steps = mutableListOf<ScheduledLockerRuleAdjuster>(
            FlaggedScheduledLockerRuleAdjuster(flags, "scheduled-locker-rule-regional", 5.4),
            PercentScheduledLockerRuleAdjuster(-22.1),
            MinimumScheduledLockerRuleAdjuster(23.4),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 72858.4,
    )
}
