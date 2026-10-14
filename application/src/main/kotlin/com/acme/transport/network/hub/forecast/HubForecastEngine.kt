package com.acme.transport.network.hub.forecast

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the hub forecast calculation. */
interface HubForecastAdjuster {
    fun describe(): String
    fun adjust(item: HubForecast, value: Double): Double
}

data class HubForecastFloorValueOffsetAdjuster(var weight: Double) : HubForecastAdjuster {
    override fun describe(): String = "plus floor value times $weight"

    override fun adjust(item: HubForecast, value: Double): Double = value + item.floorValue * weight
}

data class CapHubForecastAdjuster(var cap: Double) : HubForecastAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: HubForecast, value: Double): Double = minOf(value, cap)
}

data class RoundingHubForecastAdjuster(var step: Double) : HubForecastAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: HubForecast, value: Double): Double = roundTo(value, step)
}

/** Runs the hub forecast steps in order and keeps a trace of each. */
data class HubForecastEngine(
    var steps: MutableList<HubForecastAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: HubForecast): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<HubForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultHubForecastEngine(): HubForecastEngine {
    return HubForecastEngine(
        steps = mutableListOf<HubForecastAdjuster>(
            HubForecastFloorValueOffsetAdjuster(0.02),
            CapHubForecastAdjuster(10665.0),
            RoundingHubForecastAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 63938.2,
    )
}
