package com.acme.transport.safety.scorecard.quota

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the global scorecard budget calculation. */
interface GlobalScorecardBudgetStep {
    fun describe(): String
    fun adjust(item: GlobalScorecardBudget, value: Double): Double
}

data class MinimumGlobalScorecardBudgetAdjuster(var minimum: Double) : GlobalScorecardBudgetStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalScorecardBudget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingGlobalScorecardBudgetAdjuster(var step: Double) : GlobalScorecardBudgetStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalScorecardBudget, value: Double): Double = roundTo(value, step)
}

data class TieredGlobalScorecardBudgetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalScorecardBudgetStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalScorecardBudget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LocaleGlobalScorecardBudgetAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalScorecardBudgetStep {
    override fun describe(): String = "by locale"

    override fun adjust(item: GlobalScorecardBudget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentGlobalScorecardBudgetAdjuster(var percent: Double) : GlobalScorecardBudgetStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalScorecardBudget, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the global scorecard budget steps in order and keeps a trace of each. */
data class GlobalScorecardBudgetEngine(
    var steps: MutableList<GlobalScorecardBudgetStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: GlobalScorecardBudget): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<GlobalScorecardBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultGlobalScorecardBudgetEngine(): GlobalScorecardBudgetEngine {
    return GlobalScorecardBudgetEngine(
        steps = mutableListOf<GlobalScorecardBudgetStep>(
            MinimumGlobalScorecardBudgetAdjuster(5.8),
            RoundingGlobalScorecardBudgetAdjuster(1.0),
            TieredGlobalScorecardBudgetAdjuster(
                mutableListOf<Double>(2395.0, 3678.0),
                mutableListOf<Double>(1.18, 1.13),
            ),
            LocaleGlobalScorecardBudgetAdjuster(mutableMapOf<String, Double>("west" to 1.14)),
            PercentGlobalScorecardBudgetAdjuster(-6.7),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 63128.9,
    )
}
