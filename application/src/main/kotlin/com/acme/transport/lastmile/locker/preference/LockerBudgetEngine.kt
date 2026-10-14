package com.acme.transport.lastmile.locker.preference

import com.acme.transport.kit.numbers.roundTo

/** One step of the locker budget calculation. */
interface LockerBudgetStep {
    fun describe(): String
    fun adjust(item: LockerBudget, value: Double): Double
}

data class MinimumLockerBudgetAdjuster(var minimum: Double) : LockerBudgetStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LockerBudget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredLockerBudgetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LockerBudgetStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LockerBudget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CountryLockerBudgetAdjuster(var factors: MutableMap<String, Double>) : LockerBudgetStep {
    override fun describe(): String = "by country"

    override fun adjust(item: LockerBudget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingLockerBudgetAdjuster(var step: Double) : LockerBudgetStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LockerBudget, value: Double): Double = roundTo(value, step)
}

data class LockerBudgetCostOffsetAdjuster(var weight: Double) : LockerBudgetStep {
    override fun describe(): String = "plus cost times $weight"

    override fun adjust(item: LockerBudget, value: Double): Double = value + item.cost * weight
}

data class CapLockerBudgetAdjuster(var cap: Double) : LockerBudgetStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LockerBudget, value: Double): Double = minOf(value, cap)
}

/** Runs the locker budget steps in order and keeps a trace of each. */
data class LockerBudgetEngine(
    var steps: MutableList<LockerBudgetStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LockerBudget): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LockerBudgetStep = steps[index]
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

    fun evaluateAll(items: MutableList<LockerBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLockerBudgetEngine(): LockerBudgetEngine {
    return LockerBudgetEngine(
        steps = mutableListOf<LockerBudgetStep>(
            MinimumLockerBudgetAdjuster(13.2),
            TieredLockerBudgetAdjuster(
                mutableListOf<Double>(5920.0, 7379.0, 9655.0),
                mutableListOf<Double>(1.19, 1.06, 0.94),
            ),
            CountryLockerBudgetAdjuster(mutableMapOf<String, Double>("north" to 0.86)),
            RoundingLockerBudgetAdjuster(1.0),
            LockerBudgetCostOffsetAdjuster(1.62),
            CapLockerBudgetAdjuster(9700.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 31397.6,
    )
}
