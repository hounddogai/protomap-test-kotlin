package com.acme.transport.network.servicearea.areabatch

import com.acme.transport.kit.flags.Registry

/** One step of the fallback service area forecast calculation. */
interface FallbackServiceAreaForecastStep {
    fun describe(): String
    fun adjust(item: FallbackServiceAreaForecast, value: Double): Double
}

data class FlaggedFallbackServiceAreaForecastAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackServiceAreaForecastStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackServiceAreaForecast, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentFallbackServiceAreaForecastAdjuster(var percent: Double) : FallbackServiceAreaForecastStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackServiceAreaForecast, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapFallbackServiceAreaForecastAdjuster(var cap: Double) : FallbackServiceAreaForecastStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackServiceAreaForecast, value: Double): Double = minOf(value, cap)
}

data class FallbackServiceAreaForecastExpiresAtOffsetAdjuster(
    var weight: Double,
) : FallbackServiceAreaForecastStep {
    override fun describe(): String = "plus expires at times $weight"

    override fun adjust(item: FallbackServiceAreaForecast, value: Double): Double {
        return value + item.expiresAt.toDouble() * weight
    }
}

/** Runs the fallback service area forecast steps in order and keeps a trace of each. */
data class FallbackServiceAreaForecastEngine(
    var steps: MutableList<FallbackServiceAreaForecastStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: FallbackServiceAreaForecast): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackServiceAreaForecastStep = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackServiceAreaForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultFallbackServiceAreaForecastEngine(flags: Registry): FallbackServiceAreaForecastEngine {
    return FallbackServiceAreaForecastEngine(
        steps = mutableListOf<FallbackServiceAreaForecastStep>(
            FlaggedFallbackServiceAreaForecastAdjuster(flags, "fallback-service-area-forecast-legacy", -2.5),
            PercentFallbackServiceAreaForecastAdjuster(-23.5),
            CapFallbackServiceAreaForecastAdjuster(49113.0),
            FallbackServiceAreaForecastExpiresAtOffsetAdjuster(0.88),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 19794.6,
    )
}
