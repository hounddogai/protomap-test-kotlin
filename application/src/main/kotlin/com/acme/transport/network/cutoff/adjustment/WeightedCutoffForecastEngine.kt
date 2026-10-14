package com.acme.transport.network.cutoff.adjustment

import com.acme.transport.kit.numbers.roundTo

/** One step of the weighted cutoff forecast calculation. */
interface WeightedCutoffForecastAdjuster {
    fun describe(): String
    fun adjust(item: WeightedCutoffForecast, value: Double): Double
}

data class CategoryWeightedCutoffForecastAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedCutoffForecastAdjuster {
    override fun describe(): String = "by category"

    override fun adjust(item: WeightedCutoffForecast, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class WeightedCutoffForecastUpdatedAtOffsetAdjuster(var weight: Double) : WeightedCutoffForecastAdjuster {
    override fun describe(): String = "plus updated at times $weight"

    override fun adjust(item: WeightedCutoffForecast, value: Double): Double {
        return value + item.updatedAt.toDouble() * weight
    }
}

data class RoundingWeightedCutoffForecastAdjuster(var step: Double) : WeightedCutoffForecastAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedCutoffForecast, value: Double): Double = roundTo(value, step)
}

/** Runs the weighted cutoff forecast steps in order and keeps a trace of each. */
data class WeightedCutoffForecastEngine(
    var steps: MutableList<WeightedCutoffForecastAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: WeightedCutoffForecast): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedCutoffForecastAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedCutoffForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultWeightedCutoffForecastEngine(): WeightedCutoffForecastEngine {
    return WeightedCutoffForecastEngine(
        steps = mutableListOf<WeightedCutoffForecastAdjuster>(
            CategoryWeightedCutoffForecastAdjuster(
                mutableMapOf<String, Double>("central" to 1.12, "west" to 0.89),
            ),
            WeightedCutoffForecastUpdatedAtOffsetAdjuster(1.07),
            RoundingWeightedCutoffForecastAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 69560.5,
    )
}
