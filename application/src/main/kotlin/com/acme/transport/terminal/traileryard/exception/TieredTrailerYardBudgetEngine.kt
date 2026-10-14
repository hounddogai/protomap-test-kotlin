package com.acme.transport.terminal.traileryard.exception

import com.acme.transport.kit.numbers.roundTo

/** One step of the tiered trailer yard budget calculation. */
interface TieredTrailerYardBudgetModifier {
    fun describe(): String
    fun adjust(item: TieredTrailerYardBudget, value: Double): Double
}

data class MinimumTieredTrailerYardBudgetAdjuster(var minimum: Double) : TieredTrailerYardBudgetModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredTrailerYardBudget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredTieredTrailerYardBudgetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredTrailerYardBudgetModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredTrailerYardBudget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingTieredTrailerYardBudgetAdjuster(var step: Double) : TieredTrailerYardBudgetModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredTrailerYardBudget, value: Double): Double = roundTo(value, step)
}

/** Runs the tiered trailer yard budget steps in order and keeps a trace of each. */
data class TieredTrailerYardBudgetEngine(
    var steps: MutableList<TieredTrailerYardBudgetModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: TieredTrailerYardBudget): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TieredTrailerYardBudgetModifier = steps[index]
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

    fun evaluateAll(items: MutableList<TieredTrailerYardBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultTieredTrailerYardBudgetEngine(): TieredTrailerYardBudgetEngine {
    return TieredTrailerYardBudgetEngine(
        steps = mutableListOf<TieredTrailerYardBudgetModifier>(
            MinimumTieredTrailerYardBudgetAdjuster(6.3),
            TieredTieredTrailerYardBudgetAdjuster(
                mutableListOf<Double>(2738.0, 7359.0, 7377.0, 9938.0),
                mutableListOf<Double>(0.98, 0.9, 0.91, 0.87),
            ),
            RoundingTieredTrailerYardBudgetAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 25423.9,
    )
}
