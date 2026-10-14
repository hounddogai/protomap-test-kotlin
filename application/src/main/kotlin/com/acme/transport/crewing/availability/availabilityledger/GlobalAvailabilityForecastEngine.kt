package com.acme.transport.crewing.availability.availabilityledger

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the global availability forecast calculation. */
interface GlobalAvailabilityForecastAdjuster {
    fun describe(): String
    fun adjust(item: GlobalAvailabilityForecast, value: Double): Double
}

data class RoundingGlobalAvailabilityForecastAdjuster(var step: Double) : GlobalAvailabilityForecastAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalAvailabilityForecast, value: Double): Double = roundTo(value, step)
}

data class GlobalAvailabilityForecastAttemptsOffsetAdjuster(
    var weight: Double,
) : GlobalAvailabilityForecastAdjuster {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: GlobalAvailabilityForecast, value: Double): Double {
        return value + item.attempts.toDouble() * weight
    }
}

data class CapGlobalAvailabilityForecastAdjuster(var cap: Double) : GlobalAvailabilityForecastAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalAvailabilityForecast, value: Double): Double = minOf(value, cap)
}

/** Runs the global availability forecast steps in order and keeps a trace of each. */
data class GlobalAvailabilityForecastEngine(
    var steps: MutableList<GlobalAvailabilityForecastAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: GlobalAvailabilityForecast): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<GlobalAvailabilityForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultGlobalAvailabilityForecastEngine(): GlobalAvailabilityForecastEngine {
    return GlobalAvailabilityForecastEngine(
        steps = mutableListOf<GlobalAvailabilityForecastAdjuster>(
            RoundingGlobalAvailabilityForecastAdjuster(0.25),
            GlobalAvailabilityForecastAttemptsOffsetAdjuster(0.88),
            CapGlobalAvailabilityForecastAdjuster(31444.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 24983.9,
    )
}
