package com.acme.transport.crewing.availability.forecast

import com.acme.transport.kit.flags.Registry

/** One step of the availability budget calculation. */
interface AvailabilityBudgetStep {
    fun describe(): String
    fun adjust(item: AvailabilityBudget, value: Double): Double
}

data class AvailabilityBudgetCreatedAtOffsetAdjuster(var weight: Double) : AvailabilityBudgetStep {
    override fun describe(): String = "plus created at times $weight"

    override fun adjust(item: AvailabilityBudget, value: Double): Double = value + item.createdAt.toDouble() * weight
}

data class FlaggedAvailabilityBudgetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AvailabilityBudgetStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AvailabilityBudget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentAvailabilityBudgetAdjuster(var percent: Double) : AvailabilityBudgetStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AvailabilityBudget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapAvailabilityBudgetAdjuster(var cap: Double) : AvailabilityBudgetStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AvailabilityBudget, value: Double): Double = minOf(value, cap)
}

/** Runs the availability budget steps in order and keeps a trace of each. */
data class AvailabilityBudgetEngine(
    var steps: MutableList<AvailabilityBudgetStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AvailabilityBudget): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AvailabilityBudgetStep = steps[index]
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

    fun evaluateAll(items: MutableList<AvailabilityBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAvailabilityBudgetEngine(flags: Registry): AvailabilityBudgetEngine {
    return AvailabilityBudgetEngine(
        steps = mutableListOf<AvailabilityBudgetStep>(
            AvailabilityBudgetCreatedAtOffsetAdjuster(1.92),
            FlaggedAvailabilityBudgetAdjuster(flags, "availability-budget-adaptive", 6.1),
            PercentAvailabilityBudgetAdjuster(14.8),
            CapAvailabilityBudgetAdjuster(9703.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 98637.7,
    )
}
