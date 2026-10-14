package com.acme.transport.network.zone.forecast

import com.acme.transport.kit.flags.Registry

/** One step of the zone forecast calculation. */
interface ZoneForecastAdjuster {
    fun describe(): String
    fun adjust(item: ZoneForecast, value: Double): Double
}

data class FlaggedZoneForecastAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ZoneForecastAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ZoneForecast, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapZoneForecastAdjuster(var cap: Double) : ZoneForecastAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ZoneForecast, value: Double): Double = minOf(value, cap)
}

data class PercentZoneForecastAdjuster(var percent: Double) : ZoneForecastAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ZoneForecast, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class ZoneForecastPalletsOffsetAdjuster(var weight: Double) : ZoneForecastAdjuster {
    override fun describe(): String = "plus pallets times $weight"

    override fun adjust(item: ZoneForecast, value: Double): Double = value + item.pallets.toDouble() * weight
}

data class TieredZoneForecastAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ZoneForecastAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ZoneForecast, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class DepotZoneForecastAdjuster(var factors: MutableMap<String, Double>) : ZoneForecastAdjuster {
    override fun describe(): String = "by depot"

    override fun adjust(item: ZoneForecast, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the zone forecast steps in order and keeps a trace of each. */
data class ZoneForecastEngine(
    var steps: MutableList<ZoneForecastAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ZoneForecast): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ZoneForecastAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ZoneForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultZoneForecastEngine(flags: Registry): ZoneForecastEngine {
    return ZoneForecastEngine(
        steps = mutableListOf<ZoneForecastAdjuster>(
            FlaggedZoneForecastAdjuster(flags, "zone-forecast-legacy", -6.6),
            CapZoneForecastAdjuster(48242.0),
            PercentZoneForecastAdjuster(-17.4),
            ZoneForecastPalletsOffsetAdjuster(1.92),
            TieredZoneForecastAdjuster(mutableListOf<Double>(5044.0, 9889.0), mutableListOf<Double>(1.01, 1.04)),
            DepotZoneForecastAdjuster(mutableMapOf<String, Double>("west" to 0.71, "online" to 0.71)),
        ),
        trace = mutableListOf<String>(),
        floor = 5.0,
        ceiling = 16116.5,
    )
}
