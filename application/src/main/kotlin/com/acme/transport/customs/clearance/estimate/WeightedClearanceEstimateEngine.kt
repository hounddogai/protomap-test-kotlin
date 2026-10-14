package com.acme.transport.customs.clearance.estimate

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the weighted clearance estimate calculation. */
interface WeightedClearanceEstimateAdjuster {
    fun describe(): String
    fun adjust(item: WeightedClearanceEstimate, value: Double): Double
}

data class NameWeightedClearanceEstimateAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedClearanceEstimateAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: WeightedClearanceEstimate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredWeightedClearanceEstimateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedClearanceEstimateAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedClearanceEstimate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class WeightedClearanceEstimateAttemptsOffsetAdjuster(
    var weight: Double,
) : WeightedClearanceEstimateAdjuster {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: WeightedClearanceEstimate, value: Double): Double {
        return value + item.attempts.toDouble() * weight
    }
}

data class RoundingWeightedClearanceEstimateAdjuster(var step: Double) : WeightedClearanceEstimateAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedClearanceEstimate, value: Double): Double = roundTo(value, step)
}

/** Runs the weighted clearance estimate steps in order and keeps a trace of each. */
data class WeightedClearanceEstimateEngine(
    var steps: MutableList<WeightedClearanceEstimateAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: WeightedClearanceEstimate): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<WeightedClearanceEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultWeightedClearanceEstimateEngine(): WeightedClearanceEstimateEngine {
    return WeightedClearanceEstimateEngine(
        steps = mutableListOf<WeightedClearanceEstimateAdjuster>(
            NameWeightedClearanceEstimateAdjuster(mutableMapOf<String, Double>("west" to 0.73)),
            TieredWeightedClearanceEstimateAdjuster(
                mutableListOf<Double>(2875.0, 6799.0, 8957.0),
                mutableListOf<Double>(1.18, 0.95, 1.11),
            ),
            WeightedClearanceEstimateAttemptsOffsetAdjuster(0.25),
            RoundingWeightedClearanceEstimateAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 26367.0,
    )
}
