package com.acme.transport.emissions.fuelburn.audit

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the weighted fuel burn target calculation. */
interface WeightedFuelBurnTargetModifier {
    fun describe(): String
    fun adjust(item: WeightedFuelBurnTarget, value: Double): Double
}

data class TieredWeightedFuelBurnTargetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedFuelBurnTargetModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedFuelBurnTarget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingWeightedFuelBurnTargetAdjuster(var step: Double) : WeightedFuelBurnTargetModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedFuelBurnTarget, value: Double): Double = roundTo(value, step)
}

data class MinimumWeightedFuelBurnTargetAdjuster(var minimum: Double) : WeightedFuelBurnTargetModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedFuelBurnTarget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapWeightedFuelBurnTargetAdjuster(var cap: Double) : WeightedFuelBurnTargetModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedFuelBurnTarget, value: Double): Double = minOf(value, cap)
}

/** Runs the weighted fuel burn target steps in order and keeps a trace of each. */
data class WeightedFuelBurnTargetEngine(
    var steps: MutableList<WeightedFuelBurnTargetModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: WeightedFuelBurnTarget): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<WeightedFuelBurnTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultWeightedFuelBurnTargetEngine(): WeightedFuelBurnTargetEngine {
    return WeightedFuelBurnTargetEngine(
        steps = mutableListOf<WeightedFuelBurnTargetModifier>(
            TieredWeightedFuelBurnTargetAdjuster(
                mutableListOf<Double>(602.0, 4183.0, 5814.0, 6290.0),
                mutableListOf<Double>(0.89, 1.05, 1.12, 0.98),
            ),
            RoundingWeightedFuelBurnTargetAdjuster(1.0),
            MinimumWeightedFuelBurnTargetAdjuster(10.7),
            CapWeightedFuelBurnTargetAdjuster(29984.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 84063.4,
    )
}
