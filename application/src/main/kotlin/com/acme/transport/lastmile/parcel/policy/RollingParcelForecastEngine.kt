package com.acme.transport.lastmile.parcel.policy

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the rolling parcel forecast calculation. */
interface RollingParcelForecastStep {
    fun describe(): String
    fun adjust(item: RollingParcelForecast, value: Double): Double
}

data class CapRollingParcelForecastAdjuster(var cap: Double) : RollingParcelForecastStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingParcelForecast, value: Double): Double = minOf(value, cap)
}

data class TieredRollingParcelForecastAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingParcelForecastStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingParcelForecast, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RollingParcelForecastRatioOffsetAdjuster(var weight: Double) : RollingParcelForecastStep {
    override fun describe(): String = "plus ratio times $weight"

    override fun adjust(item: RollingParcelForecast, value: Double): Double = value + item.ratio * weight
}

data class TitleRollingParcelForecastAdjuster(
    var factors: MutableMap<String, Double>,
) : RollingParcelForecastStep {
    override fun describe(): String = "by title"

    override fun adjust(item: RollingParcelForecast, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumRollingParcelForecastAdjuster(var minimum: Double) : RollingParcelForecastStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingParcelForecast, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the rolling parcel forecast steps in order and keeps a trace of each. */
data class RollingParcelForecastEngine(
    var steps: MutableList<RollingParcelForecastStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RollingParcelForecast): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RollingParcelForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRollingParcelForecastEngine(): RollingParcelForecastEngine {
    return RollingParcelForecastEngine(
        steps = mutableListOf<RollingParcelForecastStep>(
            CapRollingParcelForecastAdjuster(8923.0),
            TieredRollingParcelForecastAdjuster(
                mutableListOf<Double>(3541.0, 6850.0, 8716.0),
                mutableListOf<Double>(0.91, 1.19, 0.82),
            ),
            RollingParcelForecastRatioOffsetAdjuster(1.28),
            TitleRollingParcelForecastAdjuster(
                mutableMapOf<String, Double>("central" to 0.76, "south" to 1.05, "east" to 0.93),
            ),
            MinimumRollingParcelForecastAdjuster(6.8),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 70957.1,
    )
}
