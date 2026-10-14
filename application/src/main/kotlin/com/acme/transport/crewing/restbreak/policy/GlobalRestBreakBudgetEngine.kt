package com.acme.transport.crewing.restbreak.policy

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the global rest break budget calculation. */
interface GlobalRestBreakBudgetFactor {
    fun describe(): String
    fun adjust(item: GlobalRestBreakBudget, value: Double): Double
}

data class MinimumGlobalRestBreakBudgetAdjuster(var minimum: Double) : GlobalRestBreakBudgetFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalRestBreakBudget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class GlobalRestBreakBudgetStopCountOffsetAdjuster(var weight: Double) : GlobalRestBreakBudgetFactor {
    override fun describe(): String = "plus stop count times $weight"

    override fun adjust(item: GlobalRestBreakBudget, value: Double): Double {
        return value + item.stopCount.toDouble() * weight
    }
}

data class FlaggedGlobalRestBreakBudgetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalRestBreakBudgetFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalRestBreakBudget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingGlobalRestBreakBudgetAdjuster(var step: Double) : GlobalRestBreakBudgetFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalRestBreakBudget, value: Double): Double = roundTo(value, step)
}

/** Runs the global rest break budget steps in order and keeps a trace of each. */
data class GlobalRestBreakBudgetEngine(
    var steps: MutableList<GlobalRestBreakBudgetFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: GlobalRestBreakBudget): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalRestBreakBudgetFactor = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalRestBreakBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultGlobalRestBreakBudgetEngine(flags: Registry): GlobalRestBreakBudgetEngine {
    return GlobalRestBreakBudgetEngine(
        steps = mutableListOf<GlobalRestBreakBudgetFactor>(
            MinimumGlobalRestBreakBudgetAdjuster(3.9),
            GlobalRestBreakBudgetStopCountOffsetAdjuster(1.21),
            FlaggedGlobalRestBreakBudgetAdjuster(flags, "global-rest-break-budget-lenient", -9.8),
            RoundingGlobalRestBreakBudgetAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 88208.9,
    )
}
