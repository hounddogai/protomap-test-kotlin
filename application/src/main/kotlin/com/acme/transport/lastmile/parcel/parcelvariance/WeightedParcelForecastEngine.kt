package com.acme.transport.lastmile.parcel.parcelvariance

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the weighted parcel forecast calculation. */
interface WeightedParcelForecastFactor {
    fun describe(): String
    fun adjust(item: WeightedParcelForecast, value: Double): Double
}

data class WeightedParcelForecastExpiresAtOffsetAdjuster(var weight: Double) : WeightedParcelForecastFactor {
    override fun describe(): String = "plus expires at times $weight"

    override fun adjust(item: WeightedParcelForecast, value: Double): Double {
        return value + item.expiresAt.toDouble() * weight
    }
}

data class RoundingWeightedParcelForecastAdjuster(var step: Double) : WeightedParcelForecastFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedParcelForecast, value: Double): Double = roundTo(value, step)
}

data class PercentWeightedParcelForecastAdjuster(var percent: Double) : WeightedParcelForecastFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedParcelForecast, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedWeightedParcelForecastAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedParcelForecastFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedParcelForecast, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredWeightedParcelForecastAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedParcelForecastFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedParcelForecast, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the weighted parcel forecast steps in order and keeps a trace of each. */
data class WeightedParcelForecastEngine(
    var steps: MutableList<WeightedParcelForecastFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: WeightedParcelForecast): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<WeightedParcelForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultWeightedParcelForecastEngine(flags: Registry): WeightedParcelForecastEngine {
    return WeightedParcelForecastEngine(
        steps = mutableListOf<WeightedParcelForecastFactor>(
            WeightedParcelForecastExpiresAtOffsetAdjuster(1.85),
            RoundingWeightedParcelForecastAdjuster(5.0),
            PercentWeightedParcelForecastAdjuster(-11.8),
            FlaggedWeightedParcelForecastAdjuster(flags, "weighted-parcel-forecast-strict", -6.4),
            TieredWeightedParcelForecastAdjuster(
                mutableListOf<Double>(4029.0, 4216.0, 5661.0),
                mutableListOf<Double>(0.95, 1.17, 1.17),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 48791.2,
    )
}
