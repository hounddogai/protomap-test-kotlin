package com.acme.transport.network.cutoff.estimate

import com.acme.transport.kit.numbers.roundTo

/** One step of the weighted cutoff estimate calculation. */
interface WeightedCutoffEstimateModifier {
    fun describe(): String
    fun adjust(item: WeightedCutoffEstimate, value: Double): Double
}

data class WeightedCutoffEstimateCostOffsetAdjuster(var weight: Double) : WeightedCutoffEstimateModifier {
    override fun describe(): String = "plus cost times $weight"

    override fun adjust(item: WeightedCutoffEstimate, value: Double): Double = value + item.cost * weight
}

data class RoundingWeightedCutoffEstimateAdjuster(var step: Double) : WeightedCutoffEstimateModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedCutoffEstimate, value: Double): Double = roundTo(value, step)
}

data class CapWeightedCutoffEstimateAdjuster(var cap: Double) : WeightedCutoffEstimateModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedCutoffEstimate, value: Double): Double = minOf(value, cap)
}

data class TieredWeightedCutoffEstimateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedCutoffEstimateModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedCutoffEstimate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumWeightedCutoffEstimateAdjuster(var minimum: Double) : WeightedCutoffEstimateModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedCutoffEstimate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the weighted cutoff estimate steps in order and keeps a trace of each. */
data class WeightedCutoffEstimateEngine(
    var steps: MutableList<WeightedCutoffEstimateModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: WeightedCutoffEstimate): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedCutoffEstimateModifier = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedCutoffEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultWeightedCutoffEstimateEngine(): WeightedCutoffEstimateEngine {
    return WeightedCutoffEstimateEngine(
        steps = mutableListOf<WeightedCutoffEstimateModifier>(
            WeightedCutoffEstimateCostOffsetAdjuster(0.12),
            RoundingWeightedCutoffEstimateAdjuster(0.1),
            CapWeightedCutoffEstimateAdjuster(13270.0),
            TieredWeightedCutoffEstimateAdjuster(
                mutableListOf<Double>(1392.0, 2294.0, 6204.0),
                mutableListOf<Double>(0.82, 0.95, 1.16),
            ),
            MinimumWeightedCutoffEstimateAdjuster(16.7),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 54848.4,
    )
}
