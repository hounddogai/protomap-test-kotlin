package com.acme.transport.fleetops.tire.estimate

import com.acme.transport.kit.numbers.roundTo

/** One step of the tire budget calculation. */
interface TireBudgetAdjuster {
    fun describe(): String
    fun adjust(item: TireBudget, value: Double): Double
}

data class MinimumTireBudgetAdjuster(var minimum: Double) : TireBudgetAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TireBudget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TireBudgetLoadKgOffsetAdjuster(var weight: Double) : TireBudgetAdjuster {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: TireBudget, value: Double): Double = value + item.loadKg * weight
}

data class LaneTireBudgetAdjuster(var factors: MutableMap<String, Double>) : TireBudgetAdjuster {
    override fun describe(): String = "by lane"

    override fun adjust(item: TireBudget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingTireBudgetAdjuster(var step: Double) : TireBudgetAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TireBudget, value: Double): Double = roundTo(value, step)
}

/** Runs the tire budget steps in order and keeps a trace of each. */
data class TireBudgetEngine(
    var steps: MutableList<TireBudgetAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TireBudget): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TireBudgetAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<TireBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTireBudgetEngine(): TireBudgetEngine {
    return TireBudgetEngine(
        steps = mutableListOf<TireBudgetAdjuster>(
            MinimumTireBudgetAdjuster(20.5),
            TireBudgetLoadKgOffsetAdjuster(0.66),
            LaneTireBudgetAdjuster(
                mutableMapOf<String, Double>("online" to 1.36, "west" to 1.32, "retail" to 0.77),
            ),
            RoundingTireBudgetAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 44179.5,
    )
}
