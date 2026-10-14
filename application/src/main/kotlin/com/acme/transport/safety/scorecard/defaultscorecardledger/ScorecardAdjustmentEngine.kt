package com.acme.transport.safety.scorecard.defaultscorecardledger

import com.acme.transport.kit.numbers.roundTo

/** One step of the scorecard adjustment calculation. */
interface ScorecardAdjustmentModifier {
    fun describe(): String
    fun adjust(item: ScorecardAdjustment, value: Double): Double
}

data class RoundingScorecardAdjustmentAdjuster(var step: Double) : ScorecardAdjustmentModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScorecardAdjustment, value: Double): Double = roundTo(value, step)
}

data class PercentScorecardAdjustmentAdjuster(var percent: Double) : ScorecardAdjustmentModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScorecardAdjustment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class ScorecardAdjustmentDiscountOffsetAdjuster(var weight: Double) : ScorecardAdjustmentModifier {
    override fun describe(): String = "plus discount times $weight"

    override fun adjust(item: ScorecardAdjustment, value: Double): Double = value + item.discount * weight
}

/** Runs the scorecard adjustment steps in order and keeps a trace of each. */
data class ScorecardAdjustmentEngine(
    var steps: MutableList<ScorecardAdjustmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ScorecardAdjustment): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScorecardAdjustmentModifier = steps[index]
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

    fun evaluateAll(items: MutableList<ScorecardAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultScorecardAdjustmentEngine(): ScorecardAdjustmentEngine {
    return ScorecardAdjustmentEngine(
        steps = mutableListOf<ScorecardAdjustmentModifier>(
            RoundingScorecardAdjustmentAdjuster(0.1),
            PercentScorecardAdjustmentAdjuster(4.6),
            ScorecardAdjustmentDiscountOffsetAdjuster(1.32),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 13317.3,
    )
}
