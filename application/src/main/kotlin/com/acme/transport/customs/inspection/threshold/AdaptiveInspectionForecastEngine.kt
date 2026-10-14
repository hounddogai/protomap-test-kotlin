package com.acme.transport.customs.inspection.threshold

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the adaptive inspection forecast calculation. */
interface AdaptiveInspectionForecastAdjuster {
    fun describe(): String
    fun adjust(item: AdaptiveInspectionForecast, value: Double): Double
}

data class MinimumAdaptiveInspectionForecastAdjuster(var minimum: Double) : AdaptiveInspectionForecastAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveInspectionForecast, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredAdaptiveInspectionForecastAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveInspectionForecastAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveInspectionForecast, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingAdaptiveInspectionForecastAdjuster(var step: Double) : AdaptiveInspectionForecastAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveInspectionForecast, value: Double): Double = roundTo(value, step)
}

data class CapAdaptiveInspectionForecastAdjuster(var cap: Double) : AdaptiveInspectionForecastAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveInspectionForecast, value: Double): Double = minOf(value, cap)
}

data class PercentAdaptiveInspectionForecastAdjuster(var percent: Double) : AdaptiveInspectionForecastAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveInspectionForecast, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the adaptive inspection forecast steps in order and keeps a trace of each. */
data class AdaptiveInspectionForecastEngine(
    var steps: MutableList<AdaptiveInspectionForecastAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AdaptiveInspectionForecast): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AdaptiveInspectionForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAdaptiveInspectionForecastEngine(): AdaptiveInspectionForecastEngine {
    return AdaptiveInspectionForecastEngine(
        steps = mutableListOf<AdaptiveInspectionForecastAdjuster>(
            MinimumAdaptiveInspectionForecastAdjuster(3.3),
            TieredAdaptiveInspectionForecastAdjuster(
                mutableListOf<Double>(763.0, 1159.0, 6292.0, 7446.0),
                mutableListOf<Double>(1.03, 1.01, 1.08, 1.2),
            ),
            RoundingAdaptiveInspectionForecastAdjuster(1.0),
            CapAdaptiveInspectionForecastAdjuster(39198.0),
            PercentAdaptiveInspectionForecastAdjuster(-16.8),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 69133.3,
    )
}
