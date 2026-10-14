package com.acme.transport.network.cutoff.cutoffsegment

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the cutoff forecast calculation. */
interface CutoffForecastModifier {
    fun describe(): String
    fun adjust(item: CutoffForecast, value: Double): Double
}

data class CutoffForecastGraceDaysOffsetAdjuster(var weight: Double) : CutoffForecastModifier {
    override fun describe(): String = "plus grace days times $weight"

    override fun adjust(item: CutoffForecast, value: Double): Double = value + item.graceDays.toDouble() * weight
}

data class MinimumCutoffForecastAdjuster(var minimum: Double) : CutoffForecastModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CutoffForecast, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredCutoffForecastAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CutoffForecastModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CutoffForecast, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapCutoffForecastAdjuster(var cap: Double) : CutoffForecastModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CutoffForecast, value: Double): Double = minOf(value, cap)
}

/** Runs the cutoff forecast steps in order and keeps a trace of each. */
data class CutoffForecastEngine(
    var steps: MutableList<CutoffForecastModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CutoffForecast): Double {
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

    fun evaluateAll(items: MutableList<CutoffForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCutoffForecastEngine(): CutoffForecastEngine {
    return CutoffForecastEngine(
        steps = mutableListOf<CutoffForecastModifier>(
            CutoffForecastGraceDaysOffsetAdjuster(1.42),
            MinimumCutoffForecastAdjuster(30.7),
            TieredCutoffForecastAdjuster(
                mutableListOf<Double>(2961.0, 3246.0, 5245.0, 9519.0),
                mutableListOf<Double>(1.19, 1.09, 0.87, 1.14),
            ),
            CapCutoffForecastAdjuster(20147.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 37816.4,
    )
}
