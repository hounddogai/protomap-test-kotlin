package com.acme.transport.emissions.idling.idlingforecast

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the legacy idling forecast calculation. */
interface LegacyIdlingForecastAdjuster {
    fun describe(): String
    fun adjust(item: LegacyIdlingForecast, value: Double): Double
}

data class RoundingLegacyIdlingForecastAdjuster(var step: Double) : LegacyIdlingForecastAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyIdlingForecast, value: Double): Double = roundTo(value, step)
}

data class FlaggedLegacyIdlingForecastAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyIdlingForecastAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyIdlingForecast, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentLegacyIdlingForecastAdjuster(var percent: Double) : LegacyIdlingForecastAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyIdlingForecast, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapLegacyIdlingForecastAdjuster(var cap: Double) : LegacyIdlingForecastAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyIdlingForecast, value: Double): Double = minOf(value, cap)
}

data class MinimumLegacyIdlingForecastAdjuster(var minimum: Double) : LegacyIdlingForecastAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyIdlingForecast, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredLegacyIdlingForecastAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyIdlingForecastAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyIdlingForecast, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the legacy idling forecast steps in order and keeps a trace of each. */
data class LegacyIdlingForecastEngine(
    var steps: MutableList<LegacyIdlingForecastAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LegacyIdlingForecast): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LegacyIdlingForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLegacyIdlingForecastEngine(flags: Registry): LegacyIdlingForecastEngine {
    return LegacyIdlingForecastEngine(
        steps = mutableListOf<LegacyIdlingForecastAdjuster>(
            RoundingLegacyIdlingForecastAdjuster(0.5),
            FlaggedLegacyIdlingForecastAdjuster(flags, "legacy-idling-forecast-fallback", -2.8),
            PercentLegacyIdlingForecastAdjuster(-20.3),
            CapLegacyIdlingForecastAdjuster(19357.0),
            MinimumLegacyIdlingForecastAdjuster(18.2),
            TieredLegacyIdlingForecastAdjuster(
                mutableListOf<Double>(11.0, 2855.0, 4255.0),
                mutableListOf<Double>(1.06, 1.19, 0.95),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 20444.0,
    )
}
