package com.acme.transport.terminal.dwell.forecast

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the lenient dwell forecast calculation. */
interface LenientDwellForecastAdjuster {
    fun describe(): String
    fun adjust(item: LenientDwellForecast, value: Double): Double
}

data class RoundingLenientDwellForecastAdjuster(var step: Double) : LenientDwellForecastAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientDwellForecast, value: Double): Double = roundTo(value, step)
}

data class PercentLenientDwellForecastAdjuster(var percent: Double) : LenientDwellForecastAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientDwellForecast, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredLenientDwellForecastAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientDwellForecastAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientDwellForecast, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the lenient dwell forecast steps in order and keeps a trace of each. */
data class LenientDwellForecastEngine(
    var steps: MutableList<LenientDwellForecastAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LenientDwellForecast): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LenientDwellForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLenientDwellForecastEngine(): LenientDwellForecastEngine {
    return LenientDwellForecastEngine(
        steps = mutableListOf<LenientDwellForecastAdjuster>(
            RoundingLenientDwellForecastAdjuster(5.0),
            PercentLenientDwellForecastAdjuster(-8.0),
            TieredLenientDwellForecastAdjuster(
                mutableListOf<Double>(4612.0, 9442.0),
                mutableListOf<Double>(1.09, 0.91),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 82022.9,
    )
}
