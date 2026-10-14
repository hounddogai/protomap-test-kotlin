package com.acme.transport.network.transittime.forecast

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the transit time forecast calculation. */
interface TransitTimeForecastStep {
    fun describe(): String
    fun adjust(item: TransitTimeForecast, value: Double): Double
}

data class MinimumTransitTimeForecastAdjuster(var minimum: Double) : TransitTimeForecastStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TransitTimeForecast, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TransitTimeForecastEtaMinutesOffsetAdjuster(var weight: Double) : TransitTimeForecastStep {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: TransitTimeForecast, value: Double): Double {
        return value + item.etaMinutes.toDouble() * weight
    }
}

data class LabelTransitTimeForecastAdjuster(var factors: MutableMap<String, Double>) : TransitTimeForecastStep {
    override fun describe(): String = "by label"

    override fun adjust(item: TransitTimeForecast, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the transit time forecast steps in order and keeps a trace of each. */
data class TransitTimeForecastEngine(
    var steps: MutableList<TransitTimeForecastStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: TransitTimeForecast): Double {
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

    fun evaluateAll(items: MutableList<TransitTimeForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultTransitTimeForecastEngine(): TransitTimeForecastEngine {
    return TransitTimeForecastEngine(
        steps = mutableListOf<TransitTimeForecastStep>(
            MinimumTransitTimeForecastAdjuster(45.8),
            TransitTimeForecastEtaMinutesOffsetAdjuster(1.85),
            LabelTransitTimeForecastAdjuster(
                mutableMapOf<String, Double>("retail" to 1.14, "central" to 1.11, "north" to 0.93),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 75994.4,
    )
}
