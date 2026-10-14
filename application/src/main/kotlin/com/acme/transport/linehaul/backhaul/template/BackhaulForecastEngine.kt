package com.acme.transport.linehaul.backhaul.template

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the backhaul forecast calculation. */
interface BackhaulForecastStep {
    fun describe(): String
    fun adjust(item: BackhaulForecast, value: Double): Double
}

data class CapBackhaulForecastAdjuster(var cap: Double) : BackhaulForecastStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BackhaulForecast, value: Double): Double = minOf(value, cap)
}

data class RoundingBackhaulForecastAdjuster(var step: Double) : BackhaulForecastStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BackhaulForecast, value: Double): Double = roundTo(value, step)
}

data class TieredBackhaulForecastAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BackhaulForecastStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BackhaulForecast, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the backhaul forecast steps in order and keeps a trace of each. */
data class BackhaulForecastEngine(
    var steps: MutableList<BackhaulForecastStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: BackhaulForecast): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BackhaulForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultBackhaulForecastEngine(): BackhaulForecastEngine {
    return BackhaulForecastEngine(
        steps = mutableListOf<BackhaulForecastStep>(
            CapBackhaulForecastAdjuster(8909.0),
            RoundingBackhaulForecastAdjuster(5.0),
            TieredBackhaulForecastAdjuster(
                mutableListOf<Double>(8536.0, 8803.0),
                mutableListOf<Double>(1.17, 1.01),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 53500.7,
    )
}
