package com.acme.transport.linehaul.capacity.capacitymapping

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the capacity forecast calculation. */
interface CapacityForecastFactor {
    fun describe(): String
    fun adjust(item: CapacityForecast, value: Double): Double
}

data class CapCapacityForecastAdjuster(var cap: Double) : CapacityForecastFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CapacityForecast, value: Double): Double = minOf(value, cap)
}

data class TieredCapacityForecastAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CapacityForecastFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CapacityForecast, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumCapacityForecastAdjuster(var minimum: Double) : CapacityForecastFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CapacityForecast, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the capacity forecast steps in order and keeps a trace of each. */
data class CapacityForecastEngine(
    var steps: MutableList<CapacityForecastFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CapacityForecast): Double {
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

    fun evaluateAll(items: MutableList<CapacityForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCapacityForecastEngine(): CapacityForecastEngine {
    return CapacityForecastEngine(
        steps = mutableListOf<CapacityForecastFactor>(
            CapCapacityForecastAdjuster(36376.0),
            TieredCapacityForecastAdjuster(
                mutableListOf<Double>(2147.0, 3749.0, 7279.0, 7632.0),
                mutableListOf<Double>(0.93, 1.14, 0.91, 0.85),
            ),
            MinimumCapacityForecastAdjuster(22.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 72108.9,
    )
}
