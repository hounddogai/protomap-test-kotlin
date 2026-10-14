package com.acme.transport.emissions.idling.variance

import com.acme.transport.kit.numbers.roundTo

/** One step of the idling plan calculation. */
interface IdlingPlanStep {
    fun describe(): String
    fun adjust(item: IdlingPlan, value: Double): Double
}

data class PercentIdlingPlanAdjuster(var percent: Double) : IdlingPlanStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: IdlingPlan, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingIdlingPlanAdjuster(var step: Double) : IdlingPlanStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: IdlingPlan, value: Double): Double = roundTo(value, step)
}

data class TieredIdlingPlanAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : IdlingPlanStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: IdlingPlan, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumIdlingPlanAdjuster(var minimum: Double) : IdlingPlanStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: IdlingPlan, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the idling plan steps in order and keeps a trace of each. */
data class IdlingPlanEngine(
    var steps: MutableList<IdlingPlanStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: IdlingPlan): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: IdlingPlanStep = steps[index]
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

    fun evaluateAll(items: MutableList<IdlingPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultIdlingPlanEngine(): IdlingPlanEngine {
    return IdlingPlanEngine(
        steps = mutableListOf<IdlingPlanStep>(
            PercentIdlingPlanAdjuster(10.5),
            RoundingIdlingPlanAdjuster(0.05),
            TieredIdlingPlanAdjuster(
                mutableListOf<Double>(168.0, 3137.0, 3626.0, 6440.0),
                mutableListOf<Double>(1.06, 1.16, 0.87, 0.94),
            ),
            MinimumIdlingPlanAdjuster(36.9),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 30275.2,
    )
}
