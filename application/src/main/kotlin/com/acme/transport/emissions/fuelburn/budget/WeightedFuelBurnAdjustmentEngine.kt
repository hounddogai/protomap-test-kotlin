package com.acme.transport.emissions.fuelburn.budget

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the weighted fuel burn adjustment calculation. */
interface WeightedFuelBurnAdjustmentFactor {
    fun describe(): String
    fun adjust(item: WeightedFuelBurnAdjustment, value: Double): Double
}

data class MinimumWeightedFuelBurnAdjustmentAdjuster(var minimum: Double) : WeightedFuelBurnAdjustmentFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedFuelBurnAdjustment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingWeightedFuelBurnAdjustmentAdjuster(var step: Double) : WeightedFuelBurnAdjustmentFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedFuelBurnAdjustment, value: Double): Double = roundTo(value, step)
}

data class TieredWeightedFuelBurnAdjustmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedFuelBurnAdjustmentFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedFuelBurnAdjustment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class WeightedFuelBurnAdjustmentStartsAtOffsetAdjuster(
    var weight: Double,
) : WeightedFuelBurnAdjustmentFactor {
    override fun describe(): String = "plus starts at times $weight"

    override fun adjust(item: WeightedFuelBurnAdjustment, value: Double): Double {
        return value + item.startsAt.toDouble() * weight
    }
}

/** Runs the weighted fuel burn adjustment steps in order and keeps a trace of each. */
data class WeightedFuelBurnAdjustmentEngine(
    var steps: MutableList<WeightedFuelBurnAdjustmentFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: WeightedFuelBurnAdjustment): Double {
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

    fun evaluateAll(items: MutableList<WeightedFuelBurnAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultWeightedFuelBurnAdjustmentEngine(): WeightedFuelBurnAdjustmentEngine {
    return WeightedFuelBurnAdjustmentEngine(
        steps = mutableListOf<WeightedFuelBurnAdjustmentFactor>(
            MinimumWeightedFuelBurnAdjustmentAdjuster(44.4),
            RoundingWeightedFuelBurnAdjustmentAdjuster(5.0),
            TieredWeightedFuelBurnAdjustmentAdjuster(
                mutableListOf<Double>(2148.0, 6107.0, 6436.0),
                mutableListOf<Double>(1.17, 0.94, 1.16),
            ),
            WeightedFuelBurnAdjustmentStartsAtOffsetAdjuster(1.33),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 65754.6,
    )
}
