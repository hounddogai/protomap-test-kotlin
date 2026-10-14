package com.acme.transport.crewing.swap.window

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the adaptive swap budget calculation. */
interface AdaptiveSwapBudgetFactor {
    fun describe(): String
    fun adjust(item: AdaptiveSwapBudget, value: Double): Double
}

data class TieredAdaptiveSwapBudgetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveSwapBudgetFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveSwapBudget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapAdaptiveSwapBudgetAdjuster(var cap: Double) : AdaptiveSwapBudgetFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveSwapBudget, value: Double): Double = minOf(value, cap)
}

data class RoundingAdaptiveSwapBudgetAdjuster(var step: Double) : AdaptiveSwapBudgetFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveSwapBudget, value: Double): Double = roundTo(value, step)
}

data class AdaptiveSwapBudgetAttemptsOffsetAdjuster(var weight: Double) : AdaptiveSwapBudgetFactor {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: AdaptiveSwapBudget, value: Double): Double = value + item.attempts.toDouble() * weight
}

data class FlaggedAdaptiveSwapBudgetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveSwapBudgetFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveSwapBudget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TitleAdaptiveSwapBudgetAdjuster(var factors: MutableMap<String, Double>) : AdaptiveSwapBudgetFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: AdaptiveSwapBudget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the adaptive swap budget steps in order and keeps a trace of each. */
data class AdaptiveSwapBudgetEngine(
    var steps: MutableList<AdaptiveSwapBudgetFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AdaptiveSwapBudget): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AdaptiveSwapBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAdaptiveSwapBudgetEngine(flags: Registry): AdaptiveSwapBudgetEngine {
    return AdaptiveSwapBudgetEngine(
        steps = mutableListOf<AdaptiveSwapBudgetFactor>(
            TieredAdaptiveSwapBudgetAdjuster(
                mutableListOf<Double>(850.0, 5788.0),
                mutableListOf<Double>(1.01, 0.87),
            ),
            CapAdaptiveSwapBudgetAdjuster(34974.0),
            RoundingAdaptiveSwapBudgetAdjuster(0.25),
            AdaptiveSwapBudgetAttemptsOffsetAdjuster(1.65),
            FlaggedAdaptiveSwapBudgetAdjuster(flags, "adaptive-swap-budget-primary", -5.4),
            TitleAdaptiveSwapBudgetAdjuster(mutableMapOf<String, Double>("east" to 1.19)),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 65476.7,
    )
}
