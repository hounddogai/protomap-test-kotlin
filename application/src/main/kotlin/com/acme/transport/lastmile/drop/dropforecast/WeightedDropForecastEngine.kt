package com.acme.transport.lastmile.drop.dropforecast

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the weighted drop forecast calculation. */
interface WeightedDropForecastAdjuster {
    fun describe(): String
    fun adjust(item: WeightedDropForecast, value: Double): Double
}

data class MinimumWeightedDropForecastAdjuster(var minimum: Double) : WeightedDropForecastAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedDropForecast, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentWeightedDropForecastAdjuster(var percent: Double) : WeightedDropForecastAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedDropForecast, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class DepotWeightedDropForecastAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedDropForecastAdjuster {
    override fun describe(): String = "by depot"

    override fun adjust(item: WeightedDropForecast, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the weighted drop forecast steps in order and keeps a trace of each. */
data class WeightedDropForecastEngine(
    var steps: MutableList<WeightedDropForecastAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: WeightedDropForecast): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<WeightedDropForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultWeightedDropForecastEngine(): WeightedDropForecastEngine {
    return WeightedDropForecastEngine(
        steps = mutableListOf<WeightedDropForecastAdjuster>(
            MinimumWeightedDropForecastAdjuster(28.2),
            PercentWeightedDropForecastAdjuster(-12.0),
            DepotWeightedDropForecastAdjuster(mutableMapOf<String, Double>("retail" to 0.98, "east" to 0.8)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 6698.1,
    )
}
