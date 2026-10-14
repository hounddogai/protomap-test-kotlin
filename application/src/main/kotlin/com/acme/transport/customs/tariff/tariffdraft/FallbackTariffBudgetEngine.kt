package com.acme.transport.customs.tariff.tariffdraft

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the fallback tariff budget calculation. */
interface FallbackTariffBudgetAdjuster {
    fun describe(): String
    fun adjust(item: FallbackTariffBudget, value: Double): Double
}

data class PercentFallbackTariffBudgetAdjuster(var percent: Double) : FallbackTariffBudgetAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackTariffBudget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingFallbackTariffBudgetAdjuster(var step: Double) : FallbackTariffBudgetAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackTariffBudget, value: Double): Double = roundTo(value, step)
}

data class FlaggedFallbackTariffBudgetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackTariffBudgetAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackTariffBudget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the fallback tariff budget steps in order and keeps a trace of each. */
data class FallbackTariffBudgetEngine(
    var steps: MutableList<FallbackTariffBudgetAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: FallbackTariffBudget): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackTariffBudgetAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackTariffBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultFallbackTariffBudgetEngine(flags: Registry): FallbackTariffBudgetEngine {
    return FallbackTariffBudgetEngine(
        steps = mutableListOf<FallbackTariffBudgetAdjuster>(
            PercentFallbackTariffBudgetAdjuster(17.5),
            RoundingFallbackTariffBudgetAdjuster(1.0),
            FlaggedFallbackTariffBudgetAdjuster(flags, "fallback-tariff-budget-regional", -3.6),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 34661.8,
    )
}
