package com.acme.transport.customs.tariff.score

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the weighted tariff budget calculation. */
interface WeightedTariffBudgetFactor {
    fun describe(): String
    fun adjust(item: WeightedTariffBudget, value: Double): Double
}

data class MinimumWeightedTariffBudgetAdjuster(var minimum: Double) : WeightedTariffBudgetFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedTariffBudget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedWeightedTariffBudgetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedTariffBudgetFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedTariffBudget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class WeightedTariffBudgetRateOffsetAdjuster(var weight: Double) : WeightedTariffBudgetFactor {
    override fun describe(): String = "plus rate times $weight"

    override fun adjust(item: WeightedTariffBudget, value: Double): Double = value + item.rate * weight
}

data class RoundingWeightedTariffBudgetAdjuster(var step: Double) : WeightedTariffBudgetFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedTariffBudget, value: Double): Double = roundTo(value, step)
}

data class PercentWeightedTariffBudgetAdjuster(var percent: Double) : WeightedTariffBudgetFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedTariffBudget, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the weighted tariff budget steps in order and keeps a trace of each. */
data class WeightedTariffBudgetEngine(
    var steps: MutableList<WeightedTariffBudgetFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: WeightedTariffBudget): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedTariffBudgetFactor = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedTariffBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultWeightedTariffBudgetEngine(flags: Registry): WeightedTariffBudgetEngine {
    return WeightedTariffBudgetEngine(
        steps = mutableListOf<WeightedTariffBudgetFactor>(
            MinimumWeightedTariffBudgetAdjuster(45.7),
            FlaggedWeightedTariffBudgetAdjuster(flags, "weighted-tariff-budget-tiered", -9.6),
            WeightedTariffBudgetRateOffsetAdjuster(0.77),
            RoundingWeightedTariffBudgetAdjuster(1.0),
            PercentWeightedTariffBudgetAdjuster(-4.8),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 56889.0,
    )
}
