package com.acme.transport.terminal.traileryard.yardforecast

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the regional trailer yard forecast calculation. */
interface RegionalTrailerYardForecastFactor {
    fun describe(): String
    fun adjust(item: RegionalTrailerYardForecast, value: Double): Double
}

data class RegionalTrailerYardForecastThresholdOffsetAdjuster(
    var weight: Double,
) : RegionalTrailerYardForecastFactor {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: RegionalTrailerYardForecast, value: Double): Double = value + item.threshold * weight
}

data class CategoryRegionalTrailerYardForecastAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalTrailerYardForecastFactor {
    override fun describe(): String = "by category"

    override fun adjust(item: RegionalTrailerYardForecast, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumRegionalTrailerYardForecastAdjuster(var minimum: Double) : RegionalTrailerYardForecastFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalTrailerYardForecast, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the regional trailer yard forecast steps in order and keeps a trace of each. */
data class RegionalTrailerYardForecastEngine(
    var steps: MutableList<RegionalTrailerYardForecastFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RegionalTrailerYardForecast): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RegionalTrailerYardForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRegionalTrailerYardForecastEngine(): RegionalTrailerYardForecastEngine {
    return RegionalTrailerYardForecastEngine(
        steps = mutableListOf<RegionalTrailerYardForecastFactor>(
            RegionalTrailerYardForecastThresholdOffsetAdjuster(1.4),
            CategoryRegionalTrailerYardForecastAdjuster(
                mutableMapOf<String, Double>("west" to 1.36, "south" to 0.71),
            ),
            MinimumRegionalTrailerYardForecastAdjuster(25.2),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 57669.3,
    )
}
