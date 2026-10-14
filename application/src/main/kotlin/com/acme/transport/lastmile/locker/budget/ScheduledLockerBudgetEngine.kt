package com.acme.transport.lastmile.locker.budget

import com.acme.transport.kit.flags.Registry

/** One step of the scheduled locker budget calculation. */
interface ScheduledLockerBudgetStep {
    fun describe(): String
    fun adjust(item: ScheduledLockerBudget, value: Double): Double
}

data class TieredScheduledLockerBudgetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledLockerBudgetStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledLockerBudget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumScheduledLockerBudgetAdjuster(var minimum: Double) : ScheduledLockerBudgetStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledLockerBudget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class NameScheduledLockerBudgetAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledLockerBudgetStep {
    override fun describe(): String = "by name"

    override fun adjust(item: ScheduledLockerBudget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapScheduledLockerBudgetAdjuster(var cap: Double) : ScheduledLockerBudgetStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledLockerBudget, value: Double): Double = minOf(value, cap)
}

data class FlaggedScheduledLockerBudgetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledLockerBudgetStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledLockerBudget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the scheduled locker budget steps in order and keeps a trace of each. */
data class ScheduledLockerBudgetEngine(
    var steps: MutableList<ScheduledLockerBudgetStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ScheduledLockerBudget): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledLockerBudgetStep = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledLockerBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultScheduledLockerBudgetEngine(flags: Registry): ScheduledLockerBudgetEngine {
    return ScheduledLockerBudgetEngine(
        steps = mutableListOf<ScheduledLockerBudgetStep>(
            TieredScheduledLockerBudgetAdjuster(
                mutableListOf<Double>(1733.0, 2276.0, 4429.0, 6643.0),
                mutableListOf<Double>(1.09, 0.96, 1.13, 1.0),
            ),
            MinimumScheduledLockerBudgetAdjuster(46.5),
            NameScheduledLockerBudgetAdjuster(
                mutableMapOf<String, Double>("west" to 1.17, "south" to 1.06, "retail" to 1.05),
            ),
            CapScheduledLockerBudgetAdjuster(46074.0),
            FlaggedScheduledLockerBudgetAdjuster(flags, "scheduled-locker-budget-regional", 5.7),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 87150.7,
    )
}
