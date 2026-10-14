package com.acme.transport.freightpay.ratecard.variance

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the fallback rate card variance calculation. */
interface FallbackRateCardVarianceModifier {
    fun describe(): String
    fun adjust(item: FallbackRateCardVariance, value: Double): Double
}

data class TieredFallbackRateCardVarianceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackRateCardVarianceModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackRateCardVariance, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentFallbackRateCardVarianceAdjuster(var percent: Double) : FallbackRateCardVarianceModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackRateCardVariance, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumFallbackRateCardVarianceAdjuster(var minimum: Double) : FallbackRateCardVarianceModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackRateCardVariance, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedFallbackRateCardVarianceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackRateCardVarianceModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackRateCardVariance, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapFallbackRateCardVarianceAdjuster(var cap: Double) : FallbackRateCardVarianceModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackRateCardVariance, value: Double): Double = minOf(value, cap)
}

data class FallbackRateCardVarianceGraceDaysOffsetAdjuster(
    var weight: Double,
) : FallbackRateCardVarianceModifier {
    override fun describe(): String = "plus grace days times $weight"

    override fun adjust(item: FallbackRateCardVariance, value: Double): Double {
        return value + item.graceDays.toDouble() * weight
    }
}

/** Runs the fallback rate card variance steps in order and keeps a trace of each. */
data class FallbackRateCardVarianceEngine(
    var steps: MutableList<FallbackRateCardVarianceModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: FallbackRateCardVariance): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FallbackRateCardVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultFallbackRateCardVarianceEngine(flags: Registry): FallbackRateCardVarianceEngine {
    return FallbackRateCardVarianceEngine(
        steps = mutableListOf<FallbackRateCardVarianceModifier>(
            TieredFallbackRateCardVarianceAdjuster(
                mutableListOf<Double>(4707.0, 6343.0, 7012.0, 7180.0),
                mutableListOf<Double>(1.09, 1.12, 1.04, 1.08),
            ),
            PercentFallbackRateCardVarianceAdjuster(-5.0),
            MinimumFallbackRateCardVarianceAdjuster(6.3),
            FlaggedFallbackRateCardVarianceAdjuster(flags, "fallback-rate-card-variance-fallback", -7.0),
            CapFallbackRateCardVarianceAdjuster(24370.0),
            FallbackRateCardVarianceGraceDaysOffsetAdjuster(0.48),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 12051.5,
    )
}
