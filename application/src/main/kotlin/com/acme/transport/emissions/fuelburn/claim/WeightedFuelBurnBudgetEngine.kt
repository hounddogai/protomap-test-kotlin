package com.acme.transport.emissions.fuelburn.claim

import com.acme.transport.kit.flags.Registry

/** One step of the weighted fuel burn budget calculation. */
interface WeightedFuelBurnBudgetFactor {
    fun describe(): String
    fun adjust(item: WeightedFuelBurnBudget, value: Double): Double
}

data class CapWeightedFuelBurnBudgetAdjuster(var cap: Double) : WeightedFuelBurnBudgetFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedFuelBurnBudget, value: Double): Double = minOf(value, cap)
}

data class MinimumWeightedFuelBurnBudgetAdjuster(var minimum: Double) : WeightedFuelBurnBudgetFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedFuelBurnBudget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class WeightedFuelBurnBudgetWeightOffsetAdjuster(var weight: Double) : WeightedFuelBurnBudgetFactor {
    override fun describe(): String = "plus weight times $weight"

    override fun adjust(item: WeightedFuelBurnBudget, value: Double): Double = value + item.weight * weight
}

data class PercentWeightedFuelBurnBudgetAdjuster(var percent: Double) : WeightedFuelBurnBudgetFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedFuelBurnBudget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredWeightedFuelBurnBudgetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedFuelBurnBudgetFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedFuelBurnBudget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedWeightedFuelBurnBudgetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedFuelBurnBudgetFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedFuelBurnBudget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the weighted fuel burn budget steps in order and keeps a trace of each. */
data class WeightedFuelBurnBudgetEngine(
    var steps: MutableList<WeightedFuelBurnBudgetFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: WeightedFuelBurnBudget): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedFuelBurnBudgetFactor = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedFuelBurnBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultWeightedFuelBurnBudgetEngine(flags: Registry): WeightedFuelBurnBudgetEngine {
    return WeightedFuelBurnBudgetEngine(
        steps = mutableListOf<WeightedFuelBurnBudgetFactor>(
            CapWeightedFuelBurnBudgetAdjuster(45606.0),
            MinimumWeightedFuelBurnBudgetAdjuster(19.8),
            WeightedFuelBurnBudgetWeightOffsetAdjuster(1.86),
            PercentWeightedFuelBurnBudgetAdjuster(-7.3),
            TieredWeightedFuelBurnBudgetAdjuster(
                mutableListOf<Double>(2737.0, 3801.0, 7536.0),
                mutableListOf<Double>(1.03, 0.9, 1.12),
            ),
            FlaggedWeightedFuelBurnBudgetAdjuster(flags, "weighted-fuel-burn-budget-weighted", 4.5),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 37949.1,
    )
}
