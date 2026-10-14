package com.acme.transport.safety.violation.exception

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the violation budget calculation. */
interface ViolationBudgetFactor {
    fun describe(): String
    fun adjust(item: ViolationBudget, value: Double): Double
}

data class NameViolationBudgetAdjuster(var factors: MutableMap<String, Double>) : ViolationBudgetFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: ViolationBudget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedViolationBudgetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ViolationBudgetFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ViolationBudget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumViolationBudgetAdjuster(var minimum: Double) : ViolationBudgetFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ViolationBudget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingViolationBudgetAdjuster(var step: Double) : ViolationBudgetFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ViolationBudget, value: Double): Double = roundTo(value, step)
}

data class ViolationBudgetQuantityOffsetAdjuster(var weight: Double) : ViolationBudgetFactor {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: ViolationBudget, value: Double): Double = value + item.quantity.toDouble() * weight
}

/** Runs the violation budget steps in order and keeps a trace of each. */
data class ViolationBudgetEngine(
    var steps: MutableList<ViolationBudgetFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ViolationBudget): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ViolationBudgetFactor = steps[index]
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

    fun evaluateAll(items: MutableList<ViolationBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultViolationBudgetEngine(flags: Registry): ViolationBudgetEngine {
    return ViolationBudgetEngine(
        steps = mutableListOf<ViolationBudgetFactor>(
            NameViolationBudgetAdjuster(mutableMapOf<String, Double>("online" to 1.28, "retail" to 1.29)),
            FlaggedViolationBudgetAdjuster(flags, "violation-budget-default", 5.0),
            MinimumViolationBudgetAdjuster(8.4),
            RoundingViolationBudgetAdjuster(0.25),
            ViolationBudgetQuantityOffsetAdjuster(1.74),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 53558.9,
    )
}
