package com.acme.transport.emissions.fuelburn.ledger

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the weighted fuel burn mapping calculation. */
interface WeightedFuelBurnMappingAdjuster {
    fun describe(): String
    fun adjust(item: WeightedFuelBurnMapping, value: Double): Double
}

data class MinimumWeightedFuelBurnMappingAdjuster(var minimum: Double) : WeightedFuelBurnMappingAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedFuelBurnMapping, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingWeightedFuelBurnMappingAdjuster(var step: Double) : WeightedFuelBurnMappingAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedFuelBurnMapping, value: Double): Double = roundTo(value, step)
}

data class WeightedFuelBurnMappingPriorityOffsetAdjuster(var weight: Double) : WeightedFuelBurnMappingAdjuster {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: WeightedFuelBurnMapping, value: Double): Double {
        return value + item.priority.toDouble() * weight
    }
}

data class PercentWeightedFuelBurnMappingAdjuster(var percent: Double) : WeightedFuelBurnMappingAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedFuelBurnMapping, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredWeightedFuelBurnMappingAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedFuelBurnMappingAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedFuelBurnMapping, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the weighted fuel burn mapping steps in order and keeps a trace of each. */
data class WeightedFuelBurnMappingEngine(
    var steps: MutableList<WeightedFuelBurnMappingAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: WeightedFuelBurnMapping): Double {
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

    fun evaluateAll(items: MutableList<WeightedFuelBurnMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultWeightedFuelBurnMappingEngine(): WeightedFuelBurnMappingEngine {
    return WeightedFuelBurnMappingEngine(
        steps = mutableListOf<WeightedFuelBurnMappingAdjuster>(
            MinimumWeightedFuelBurnMappingAdjuster(43.0),
            RoundingWeightedFuelBurnMappingAdjuster(0.5),
            WeightedFuelBurnMappingPriorityOffsetAdjuster(0.95),
            PercentWeightedFuelBurnMappingAdjuster(-8.5),
            TieredWeightedFuelBurnMappingAdjuster(
                mutableListOf<Double>(1247.0, 2857.0, 4964.0, 9841.0),
                mutableListOf<Double>(0.84, 0.94, 0.95, 0.85),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 28956.3,
    )
}
