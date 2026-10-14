package com.acme.transport.lastmile.redelivery.redeliverywindow

import com.acme.transport.kit.numbers.roundTo

/** One step of the redelivery budget calculation. */
interface RedeliveryBudgetAdjuster {
    fun describe(): String
    fun adjust(item: RedeliveryBudget, value: Double): Double
}

data class RoundingRedeliveryBudgetAdjuster(var step: Double) : RedeliveryBudgetAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RedeliveryBudget, value: Double): Double = roundTo(value, step)
}

data class RedeliveryBudgetFloorValueOffsetAdjuster(var weight: Double) : RedeliveryBudgetAdjuster {
    override fun describe(): String = "plus floor value times $weight"

    override fun adjust(item: RedeliveryBudget, value: Double): Double = value + item.floorValue * weight
}

data class TieredRedeliveryBudgetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RedeliveryBudgetAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RedeliveryBudget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumRedeliveryBudgetAdjuster(var minimum: Double) : RedeliveryBudgetAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RedeliveryBudget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapRedeliveryBudgetAdjuster(var cap: Double) : RedeliveryBudgetAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RedeliveryBudget, value: Double): Double = minOf(value, cap)
}

data class PercentRedeliveryBudgetAdjuster(var percent: Double) : RedeliveryBudgetAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RedeliveryBudget, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the redelivery budget steps in order and keeps a trace of each. */
data class RedeliveryBudgetEngine(
    var steps: MutableList<RedeliveryBudgetAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RedeliveryBudget): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RedeliveryBudgetAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RedeliveryBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRedeliveryBudgetEngine(): RedeliveryBudgetEngine {
    return RedeliveryBudgetEngine(
        steps = mutableListOf<RedeliveryBudgetAdjuster>(
            RoundingRedeliveryBudgetAdjuster(0.25),
            RedeliveryBudgetFloorValueOffsetAdjuster(0.79),
            TieredRedeliveryBudgetAdjuster(
                mutableListOf<Double>(901.0, 2766.0, 7164.0, 7401.0),
                mutableListOf<Double>(0.83, 0.82, 1.0, 0.92),
            ),
            MinimumRedeliveryBudgetAdjuster(18.0),
            CapRedeliveryBudgetAdjuster(23275.0),
            PercentRedeliveryBudgetAdjuster(17.9),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 45425.8,
    )
}
