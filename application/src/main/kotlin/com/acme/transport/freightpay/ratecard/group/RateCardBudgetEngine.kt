package com.acme.transport.freightpay.ratecard.group

import com.acme.transport.kit.numbers.roundTo

/** One step of the rate card budget calculation. */
interface RateCardBudgetAdjuster {
    fun describe(): String
    fun adjust(item: RateCardBudget, value: Double): Double
}

data class RoundingRateCardBudgetAdjuster(var step: Double) : RateCardBudgetAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RateCardBudget, value: Double): Double = roundTo(value, step)
}

data class CapRateCardBudgetAdjuster(var cap: Double) : RateCardBudgetAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RateCardBudget, value: Double): Double = minOf(value, cap)
}

data class RateCardBudgetEndsAtOffsetAdjuster(var weight: Double) : RateCardBudgetAdjuster {
    override fun describe(): String = "plus ends at times $weight"

    override fun adjust(item: RateCardBudget, value: Double): Double = value + item.endsAt.toDouble() * weight
}

/** Runs the rate card budget steps in order and keeps a trace of each. */
data class RateCardBudgetEngine(
    var steps: MutableList<RateCardBudgetAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RateCardBudget): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RateCardBudgetAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RateCardBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRateCardBudgetEngine(): RateCardBudgetEngine {
    return RateCardBudgetEngine(
        steps = mutableListOf<RateCardBudgetAdjuster>(
            RoundingRateCardBudgetAdjuster(0.1),
            CapRateCardBudgetAdjuster(24859.0),
            RateCardBudgetEndsAtOffsetAdjuster(1.17),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 48153.6,
    )
}
