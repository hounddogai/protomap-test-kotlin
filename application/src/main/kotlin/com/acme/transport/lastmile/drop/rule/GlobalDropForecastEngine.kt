package com.acme.transport.lastmile.drop.rule

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the global drop forecast calculation. */
interface GlobalDropForecastAdjuster {
    fun describe(): String
    fun adjust(item: GlobalDropForecast, value: Double): Double
}

data class RoundingGlobalDropForecastAdjuster(var step: Double) : GlobalDropForecastAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalDropForecast, value: Double): Double = roundTo(value, step)
}

data class MinimumGlobalDropForecastAdjuster(var minimum: Double) : GlobalDropForecastAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalDropForecast, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentGlobalDropForecastAdjuster(var percent: Double) : GlobalDropForecastAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalDropForecast, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RegionGlobalDropForecastAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalDropForecastAdjuster {
    override fun describe(): String = "by region"

    override fun adjust(item: GlobalDropForecast, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the global drop forecast steps in order and keeps a trace of each. */
data class GlobalDropForecastEngine(
    var steps: MutableList<GlobalDropForecastAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: GlobalDropForecast): Double {
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

    fun evaluateAll(items: MutableList<GlobalDropForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultGlobalDropForecastEngine(): GlobalDropForecastEngine {
    return GlobalDropForecastEngine(
        steps = mutableListOf<GlobalDropForecastAdjuster>(
            RoundingGlobalDropForecastAdjuster(0.1),
            MinimumGlobalDropForecastAdjuster(6.5),
            PercentGlobalDropForecastAdjuster(10.9),
            RegionGlobalDropForecastAdjuster(mutableMapOf<String, Double>("south" to 1.18)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 72716.9,
    )
}
