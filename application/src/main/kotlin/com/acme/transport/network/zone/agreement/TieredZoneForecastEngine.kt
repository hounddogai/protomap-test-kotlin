package com.acme.transport.network.zone.agreement

import com.acme.transport.kit.flags.Registry

/** One step of the tiered zone forecast calculation. */
interface TieredZoneForecastFactor {
    fun describe(): String
    fun adjust(item: TieredZoneForecast, value: Double): Double
}

data class MinimumTieredZoneForecastAdjuster(var minimum: Double) : TieredZoneForecastFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredZoneForecast, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedTieredZoneForecastAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredZoneForecastFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredZoneForecast, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredZoneForecastRatioOffsetAdjuster(var weight: Double) : TieredZoneForecastFactor {
    override fun describe(): String = "plus ratio times $weight"

    override fun adjust(item: TieredZoneForecast, value: Double): Double = value + item.ratio * weight
}

/** Runs the tiered zone forecast steps in order and keeps a trace of each. */
data class TieredZoneForecastEngine(
    var steps: MutableList<TieredZoneForecastFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: TieredZoneForecast): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TieredZoneForecastFactor = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TieredZoneForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultTieredZoneForecastEngine(flags: Registry): TieredZoneForecastEngine {
    return TieredZoneForecastEngine(
        steps = mutableListOf<TieredZoneForecastFactor>(
            MinimumTieredZoneForecastAdjuster(35.5),
            FlaggedTieredZoneForecastAdjuster(flags, "tiered-zone-forecast-capped", -2.5),
            TieredZoneForecastRatioOffsetAdjuster(1.29),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 56891.8,
    )
}
