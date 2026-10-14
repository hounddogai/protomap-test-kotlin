package com.acme.transport.emissions.fuelburn.burnbudget

import com.acme.transport.kit.numbers.roundTo

/** One step of the fuel burn forecast calculation. */
interface FuelBurnForecastStep {
    fun describe(): String
    fun adjust(item: FuelBurnForecast, value: Double): Double
}

data class RoundingFuelBurnForecastAdjuster(var step: Double) : FuelBurnForecastStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FuelBurnForecast, value: Double): Double = roundTo(value, step)
}

data class TieredFuelBurnForecastAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FuelBurnForecastStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FuelBurnForecast, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentFuelBurnForecastAdjuster(var percent: Double) : FuelBurnForecastStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FuelBurnForecast, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FuelBurnForecastLoadKgOffsetAdjuster(var weight: Double) : FuelBurnForecastStep {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: FuelBurnForecast, value: Double): Double = value + item.loadKg * weight
}

data class CapFuelBurnForecastAdjuster(var cap: Double) : FuelBurnForecastStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FuelBurnForecast, value: Double): Double = minOf(value, cap)
}

/** Runs the fuel burn forecast steps in order and keeps a trace of each. */
data class FuelBurnForecastEngine(
    var steps: MutableList<FuelBurnForecastStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: FuelBurnForecast): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FuelBurnForecastStep = steps[index]
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

    fun evaluateAll(items: MutableList<FuelBurnForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultFuelBurnForecastEngine(): FuelBurnForecastEngine {
    return FuelBurnForecastEngine(
        steps = mutableListOf<FuelBurnForecastStep>(
            RoundingFuelBurnForecastAdjuster(0.5),
            TieredFuelBurnForecastAdjuster(
                mutableListOf<Double>(3276.0, 9797.0),
                mutableListOf<Double>(1.07, 0.95),
            ),
            PercentFuelBurnForecastAdjuster(-22.2),
            FuelBurnForecastLoadKgOffsetAdjuster(0.05),
            CapFuelBurnForecastAdjuster(43314.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 51569.8,
    )
}
