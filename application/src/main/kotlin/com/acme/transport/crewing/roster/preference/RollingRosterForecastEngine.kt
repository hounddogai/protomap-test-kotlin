package com.acme.transport.crewing.roster.preference

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rolling roster forecast calculation. */
interface RollingRosterForecastStep {
    fun describe(): String
    fun adjust(item: RollingRosterForecast, value: Double): Double
}

data class TieredRollingRosterForecastAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingRosterForecastStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingRosterForecast, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RollingRosterForecastQuantityOffsetAdjuster(var weight: Double) : RollingRosterForecastStep {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: RollingRosterForecast, value: Double): Double {
        return value + item.quantity.toDouble() * weight
    }
}

data class RoundingRollingRosterForecastAdjuster(var step: Double) : RollingRosterForecastStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingRosterForecast, value: Double): Double = roundTo(value, step)
}

/** Runs the rolling roster forecast steps in order and keeps a trace of each. */
data class RollingRosterForecastEngine(
    var steps: MutableList<RollingRosterForecastStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RollingRosterForecast): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RollingRosterForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRollingRosterForecastEngine(): RollingRosterForecastEngine {
    return RollingRosterForecastEngine(
        steps = mutableListOf<RollingRosterForecastStep>(
            TieredRollingRosterForecastAdjuster(
                mutableListOf<Double>(538.0, 1221.0, 1814.0),
                mutableListOf<Double>(1.04, 1.18, 1.11),
            ),
            RollingRosterForecastQuantityOffsetAdjuster(1.52),
            RoundingRollingRosterForecastAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 53343.4,
    )
}
