package com.acme.transport.freightpay.audit.forecast

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the fallback audit forecast calculation. */
interface FallbackAuditForecastFactor {
    fun describe(): String
    fun adjust(item: FallbackAuditForecast, value: Double): Double
}

data class MinimumFallbackAuditForecastAdjuster(var minimum: Double) : FallbackAuditForecastFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackAuditForecast, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapFallbackAuditForecastAdjuster(var cap: Double) : FallbackAuditForecastFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackAuditForecast, value: Double): Double = minOf(value, cap)
}

data class TieredFallbackAuditForecastAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackAuditForecastFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackAuditForecast, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the fallback audit forecast steps in order and keeps a trace of each. */
data class FallbackAuditForecastEngine(
    var steps: MutableList<FallbackAuditForecastFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: FallbackAuditForecast): Double {
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

    fun evaluateAll(items: MutableList<FallbackAuditForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultFallbackAuditForecastEngine(): FallbackAuditForecastEngine {
    return FallbackAuditForecastEngine(
        steps = mutableListOf<FallbackAuditForecastFactor>(
            MinimumFallbackAuditForecastAdjuster(10.3),
            CapFallbackAuditForecastAdjuster(12542.0),
            TieredFallbackAuditForecastAdjuster(
                mutableListOf<Double>(4581.0, 9181.0),
                mutableListOf<Double>(0.89, 1.19),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 86009.9,
    )
}
