package com.acme.transport.emissions.fuelburn.notice

import com.acme.transport.kit.numbers.roundTo

/** One step of the fuel burn mapping calculation. */
interface FuelBurnMappingAdjuster {
    fun describe(): String
    fun adjust(item: FuelBurnMapping, value: Double): Double
}

data class MinimumFuelBurnMappingAdjuster(var minimum: Double) : FuelBurnMappingAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FuelBurnMapping, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TitleFuelBurnMappingAdjuster(var factors: MutableMap<String, Double>) : FuelBurnMappingAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: FuelBurnMapping, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentFuelBurnMappingAdjuster(var percent: Double) : FuelBurnMappingAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FuelBurnMapping, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingFuelBurnMappingAdjuster(var step: Double) : FuelBurnMappingAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FuelBurnMapping, value: Double): Double = roundTo(value, step)
}

data class TieredFuelBurnMappingAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FuelBurnMappingAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FuelBurnMapping, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the fuel burn mapping steps in order and keeps a trace of each. */
data class FuelBurnMappingEngine(
    var steps: MutableList<FuelBurnMappingAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: FuelBurnMapping): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FuelBurnMappingAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<FuelBurnMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultFuelBurnMappingEngine(): FuelBurnMappingEngine {
    return FuelBurnMappingEngine(
        steps = mutableListOf<FuelBurnMappingAdjuster>(
            MinimumFuelBurnMappingAdjuster(40.4),
            TitleFuelBurnMappingAdjuster(mutableMapOf<String, Double>("retail" to 1.33)),
            PercentFuelBurnMappingAdjuster(-9.9),
            RoundingFuelBurnMappingAdjuster(0.5),
            TieredFuelBurnMappingAdjuster(
                mutableListOf<Double>(7066.0, 9340.0),
                mutableListOf<Double>(1.05, 1.17),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 23258.9,
    )
}
