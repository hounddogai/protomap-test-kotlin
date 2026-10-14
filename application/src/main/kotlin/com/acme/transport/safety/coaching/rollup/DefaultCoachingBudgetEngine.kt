package com.acme.transport.safety.coaching.rollup

import com.acme.transport.kit.numbers.roundTo

/** One step of the default coaching budget calculation. */
interface DefaultCoachingBudgetStep {
    fun describe(): String
    fun adjust(item: DefaultCoachingBudget, value: Double): Double
}

data class TieredDefaultCoachingBudgetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultCoachingBudgetStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultCoachingBudget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingDefaultCoachingBudgetAdjuster(var step: Double) : DefaultCoachingBudgetStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultCoachingBudget, value: Double): Double = roundTo(value, step)
}

data class MinimumDefaultCoachingBudgetAdjuster(var minimum: Double) : DefaultCoachingBudgetStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultCoachingBudget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the default coaching budget steps in order and keeps a trace of each. */
data class DefaultCoachingBudgetEngine(
    var steps: MutableList<DefaultCoachingBudgetStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DefaultCoachingBudget): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultCoachingBudgetStep = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultCoachingBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDefaultCoachingBudgetEngine(): DefaultCoachingBudgetEngine {
    return DefaultCoachingBudgetEngine(
        steps = mutableListOf<DefaultCoachingBudgetStep>(
            TieredDefaultCoachingBudgetAdjuster(
                mutableListOf<Double>(1844.0, 2286.0, 5957.0, 8973.0),
                mutableListOf<Double>(1.05, 0.82, 1.05, 1.06),
            ),
            RoundingDefaultCoachingBudgetAdjuster(0.25),
            MinimumDefaultCoachingBudgetAdjuster(34.8),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 25453.2,
    )
}
