package com.acme.transport.fleetops.serviceplan.checkpoint

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the capped service plan forecast calculation. */
interface CappedServicePlanForecastFactor {
    fun describe(): String
    fun adjust(item: CappedServicePlanForecast, value: Double): Double
}

data class RoundingCappedServicePlanForecastAdjuster(var step: Double) : CappedServicePlanForecastFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedServicePlanForecast, value: Double): Double = roundTo(value, step)
}

data class TieredCappedServicePlanForecastAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedServicePlanForecastFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedServicePlanForecast, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapCappedServicePlanForecastAdjuster(var cap: Double) : CappedServicePlanForecastFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedServicePlanForecast, value: Double): Double = minOf(value, cap)
}

/** Runs the capped service plan forecast steps in order and keeps a trace of each. */
data class CappedServicePlanForecastEngine(
    var steps: MutableList<CappedServicePlanForecastFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CappedServicePlanForecast): Double {
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

    fun evaluateAll(items: MutableList<CappedServicePlanForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCappedServicePlanForecastEngine(): CappedServicePlanForecastEngine {
    return CappedServicePlanForecastEngine(
        steps = mutableListOf<CappedServicePlanForecastFactor>(
            RoundingCappedServicePlanForecastAdjuster(1.0),
            TieredCappedServicePlanForecastAdjuster(
                mutableListOf<Double>(4277.0, 4671.0, 9549.0),
                mutableListOf<Double>(0.84, 1.12, 1.18),
            ),
            CapCappedServicePlanForecastAdjuster(31201.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 21999.4,
    )
}
