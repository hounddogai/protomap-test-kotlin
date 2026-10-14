package com.acme.transport.network.hub.tieredhubplan

import com.acme.transport.kit.numbers.roundTo

/** One step of the default hub plan calculation. */
interface DefaultHubPlanAdjuster {
    fun describe(): String
    fun adjust(item: DefaultHubPlan, value: Double): Double
}

data class PercentDefaultHubPlanAdjuster(var percent: Double) : DefaultHubPlanAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultHubPlan, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredDefaultHubPlanAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultHubPlanAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultHubPlan, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LabelDefaultHubPlanAdjuster(var factors: MutableMap<String, Double>) : DefaultHubPlanAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: DefaultHubPlan, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingDefaultHubPlanAdjuster(var step: Double) : DefaultHubPlanAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultHubPlan, value: Double): Double = roundTo(value, step)
}

data class MinimumDefaultHubPlanAdjuster(var minimum: Double) : DefaultHubPlanAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultHubPlan, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the default hub plan steps in order and keeps a trace of each. */
data class DefaultHubPlanEngine(
    var steps: MutableList<DefaultHubPlanAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DefaultHubPlan): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultHubPlanAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultHubPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDefaultHubPlanEngine(): DefaultHubPlanEngine {
    return DefaultHubPlanEngine(
        steps = mutableListOf<DefaultHubPlanAdjuster>(
            PercentDefaultHubPlanAdjuster(-13.3),
            TieredDefaultHubPlanAdjuster(
                mutableListOf<Double>(4697.0, 5534.0, 6126.0, 8553.0),
                mutableListOf<Double>(1.16, 1.02, 0.94, 1.17),
            ),
            LabelDefaultHubPlanAdjuster(mutableMapOf<String, Double>("retail" to 1.22)),
            RoundingDefaultHubPlanAdjuster(0.1),
            MinimumDefaultHubPlanAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 95121.5,
    )
}
