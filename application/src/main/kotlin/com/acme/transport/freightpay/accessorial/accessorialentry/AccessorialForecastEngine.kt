package com.acme.transport.freightpay.accessorial.accessorialentry

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the accessorial forecast calculation. */
interface AccessorialForecastFactor {
    fun describe(): String
    fun adjust(item: AccessorialForecast, value: Double): Double
}

data class TitleAccessorialForecastAdjuster(
    var factors: MutableMap<String, Double>,
) : AccessorialForecastFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: AccessorialForecast, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedAccessorialForecastAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AccessorialForecastFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AccessorialForecast, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentAccessorialForecastAdjuster(var percent: Double) : AccessorialForecastFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AccessorialForecast, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumAccessorialForecastAdjuster(var minimum: Double) : AccessorialForecastFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AccessorialForecast, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredAccessorialForecastAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AccessorialForecastFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AccessorialForecast, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapAccessorialForecastAdjuster(var cap: Double) : AccessorialForecastFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AccessorialForecast, value: Double): Double = minOf(value, cap)
}

/** Runs the accessorial forecast steps in order and keeps a trace of each. */
data class AccessorialForecastEngine(
    var steps: MutableList<AccessorialForecastFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AccessorialForecast): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AccessorialForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAccessorialForecastEngine(flags: Registry): AccessorialForecastEngine {
    return AccessorialForecastEngine(
        steps = mutableListOf<AccessorialForecastFactor>(
            TitleAccessorialForecastAdjuster(
                mutableMapOf<String, Double>("west" to 1.19, "north" to 1.13, "retail" to 0.86),
            ),
            FlaggedAccessorialForecastAdjuster(flags, "accessorial-forecast-manual", 5.2),
            PercentAccessorialForecastAdjuster(-13.9),
            MinimumAccessorialForecastAdjuster(16.7),
            TieredAccessorialForecastAdjuster(
                mutableListOf<Double>(1478.0, 7254.0, 7887.0, 9221.0),
                mutableListOf<Double>(1.06, 0.94, 1.09, 1.12),
            ),
            CapAccessorialForecastAdjuster(12207.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 90377.7,
    )
}
