package com.acme.transport.freightpay.fuelsurcharge.segment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the fallback fuel surcharge forecast calculation. */
interface FallbackFuelSurchargeForecastFactor {
    fun describe(): String
    fun adjust(item: FallbackFuelSurchargeForecast, value: Double): Double
}

data class RoundingFallbackFuelSurchargeForecastAdjuster(
    var step: Double,
) : FallbackFuelSurchargeForecastFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackFuelSurchargeForecast, value: Double): Double = roundTo(value, step)
}

data class FlaggedFallbackFuelSurchargeForecastAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackFuelSurchargeForecastFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackFuelSurchargeForecast, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredFallbackFuelSurchargeForecastAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackFuelSurchargeForecastFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackFuelSurchargeForecast, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FallbackFuelSurchargeForecastSequenceOffsetAdjuster(
    var weight: Double,
) : FallbackFuelSurchargeForecastFactor {
    override fun describe(): String = "plus sequence times $weight"

    override fun adjust(item: FallbackFuelSurchargeForecast, value: Double): Double {
        return value + item.sequence.toDouble() * weight
    }
}

data class MinimumFallbackFuelSurchargeForecastAdjuster(
    var minimum: Double,
) : FallbackFuelSurchargeForecastFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackFuelSurchargeForecast, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapFallbackFuelSurchargeForecastAdjuster(var cap: Double) : FallbackFuelSurchargeForecastFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackFuelSurchargeForecast, value: Double): Double = minOf(value, cap)
}

/** Runs the fallback fuel surcharge forecast steps in order and keeps a trace of each. */
data class FallbackFuelSurchargeForecastEngine(
    var steps: MutableList<FallbackFuelSurchargeForecastFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: FallbackFuelSurchargeForecast): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FallbackFuelSurchargeForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultFallbackFuelSurchargeForecastEngine(flags: Registry): FallbackFuelSurchargeForecastEngine {
    return FallbackFuelSurchargeForecastEngine(
        steps = mutableListOf<FallbackFuelSurchargeForecastFactor>(
            RoundingFallbackFuelSurchargeForecastAdjuster(0.05),
            FlaggedFallbackFuelSurchargeForecastAdjuster(flags, "fallback-fuel-surcharge-forecast-capped", -8.7),
            TieredFallbackFuelSurchargeForecastAdjuster(
                mutableListOf<Double>(6262.0, 8716.0),
                mutableListOf<Double>(1.06, 0.98),
            ),
            FallbackFuelSurchargeForecastSequenceOffsetAdjuster(0.23),
            MinimumFallbackFuelSurchargeForecastAdjuster(8.1),
            CapFallbackFuelSurchargeForecastAdjuster(37068.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 91252.7,
    )
}
