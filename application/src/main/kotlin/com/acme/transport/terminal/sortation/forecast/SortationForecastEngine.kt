package com.acme.transport.terminal.sortation.forecast

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the sortation forecast calculation. */
interface SortationForecastStep {
    fun describe(): String
    fun adjust(item: SortationForecast, value: Double): Double
}

data class SortationForecastCostOffsetAdjuster(var weight: Double) : SortationForecastStep {
    override fun describe(): String = "plus cost times $weight"

    override fun adjust(item: SortationForecast, value: Double): Double = value + item.cost * weight
}

data class MinimumSortationForecastAdjuster(var minimum: Double) : SortationForecastStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SortationForecast, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentSortationForecastAdjuster(var percent: Double) : SortationForecastStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SortationForecast, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapSortationForecastAdjuster(var cap: Double) : SortationForecastStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SortationForecast, value: Double): Double = minOf(value, cap)
}

data class FlaggedSortationForecastAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SortationForecastStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SortationForecast, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredSortationForecastAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SortationForecastStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SortationForecast, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the sortation forecast steps in order and keeps a trace of each. */
data class SortationForecastEngine(
    var steps: MutableList<SortationForecastStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: SortationForecast): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SortationForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultSortationForecastEngine(flags: Registry): SortationForecastEngine {
    return SortationForecastEngine(
        steps = mutableListOf<SortationForecastStep>(
            SortationForecastCostOffsetAdjuster(0.54),
            MinimumSortationForecastAdjuster(35.2),
            PercentSortationForecastAdjuster(-5.5),
            CapSortationForecastAdjuster(39905.0),
            FlaggedSortationForecastAdjuster(flags, "sortation-forecast-fallback", 0.5),
            TieredSortationForecastAdjuster(
                mutableListOf<Double>(591.0, 2833.0),
                mutableListOf<Double>(1.04, 1.16),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 91089.2,
    )
}
