package com.acme.transport.customs.clearance.quota

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the adaptive clearance budget calculation. */
interface AdaptiveClearanceBudgetModifier {
    fun describe(): String
    fun adjust(item: AdaptiveClearanceBudget, value: Double): Double
}

data class TieredAdaptiveClearanceBudgetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveClearanceBudgetModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveClearanceBudget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedAdaptiveClearanceBudgetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveClearanceBudgetModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveClearanceBudget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumAdaptiveClearanceBudgetAdjuster(var minimum: Double) : AdaptiveClearanceBudgetModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveClearanceBudget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentAdaptiveClearanceBudgetAdjuster(var percent: Double) : AdaptiveClearanceBudgetModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveClearanceBudget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapAdaptiveClearanceBudgetAdjuster(var cap: Double) : AdaptiveClearanceBudgetModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveClearanceBudget, value: Double): Double = minOf(value, cap)
}

data class RoundingAdaptiveClearanceBudgetAdjuster(var step: Double) : AdaptiveClearanceBudgetModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveClearanceBudget, value: Double): Double = roundTo(value, step)
}

/** Runs the adaptive clearance budget steps in order and keeps a trace of each. */
data class AdaptiveClearanceBudgetEngine(
    var steps: MutableList<AdaptiveClearanceBudgetModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AdaptiveClearanceBudget): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveClearanceBudgetModifier = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveClearanceBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAdaptiveClearanceBudgetEngine(flags: Registry): AdaptiveClearanceBudgetEngine {
    return AdaptiveClearanceBudgetEngine(
        steps = mutableListOf<AdaptiveClearanceBudgetModifier>(
            TieredAdaptiveClearanceBudgetAdjuster(
                mutableListOf<Double>(5575.0, 6353.0, 9495.0),
                mutableListOf<Double>(0.86, 0.97, 1.05),
            ),
            FlaggedAdaptiveClearanceBudgetAdjuster(flags, "adaptive-clearance-budget-weighted", 9.0),
            MinimumAdaptiveClearanceBudgetAdjuster(41.1),
            PercentAdaptiveClearanceBudgetAdjuster(6.4),
            CapAdaptiveClearanceBudgetAdjuster(49848.0),
            RoundingAdaptiveClearanceBudgetAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 11998.8,
    )
}
