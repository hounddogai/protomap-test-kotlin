package com.acme.transport.safety.coaching.coachingtier

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the lenient coaching forecast calculation. */
interface LenientCoachingForecastAdjuster {
    fun describe(): String
    fun adjust(item: LenientCoachingForecast, value: Double): Double
}

data class RoundingLenientCoachingForecastAdjuster(var step: Double) : LenientCoachingForecastAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientCoachingForecast, value: Double): Double = roundTo(value, step)
}

data class TieredLenientCoachingForecastAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientCoachingForecastAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientCoachingForecast, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentLenientCoachingForecastAdjuster(var percent: Double) : LenientCoachingForecastAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientCoachingForecast, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedLenientCoachingForecastAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientCoachingForecastAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientCoachingForecast, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the lenient coaching forecast steps in order and keeps a trace of each. */
data class LenientCoachingForecastEngine(
    var steps: MutableList<LenientCoachingForecastAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LenientCoachingForecast): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LenientCoachingForecastAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<LenientCoachingForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLenientCoachingForecastEngine(flags: Registry): LenientCoachingForecastEngine {
    return LenientCoachingForecastEngine(
        steps = mutableListOf<LenientCoachingForecastAdjuster>(
            RoundingLenientCoachingForecastAdjuster(0.25),
            TieredLenientCoachingForecastAdjuster(
                mutableListOf<Double>(587.0, 1362.0, 4983.0, 8211.0),
                mutableListOf<Double>(1.06, 1.04, 1.17, 1.05),
            ),
            PercentLenientCoachingForecastAdjuster(1.1),
            FlaggedLenientCoachingForecastAdjuster(flags, "lenient-coaching-forecast-strict", -9.3),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 68607.2,
    )
}
