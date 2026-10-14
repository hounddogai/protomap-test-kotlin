package com.acme.transport.emissions.carbonreport.allocation

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the fallback carbon report preference calculation. */
interface FallbackCarbonReportPreferenceAdjuster {
    fun describe(): String
    fun adjust(item: FallbackCarbonReportPreference, value: Double): Double
}

data class TieredFallbackCarbonReportPreferenceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackCarbonReportPreferenceAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackCarbonReportPreference, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapFallbackCarbonReportPreferenceAdjuster(var cap: Double) : FallbackCarbonReportPreferenceAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackCarbonReportPreference, value: Double): Double = minOf(value, cap)
}

data class PercentFallbackCarbonReportPreferenceAdjuster(
    var percent: Double,
) : FallbackCarbonReportPreferenceAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackCarbonReportPreference, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumFallbackCarbonReportPreferenceAdjuster(
    var minimum: Double,
) : FallbackCarbonReportPreferenceAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackCarbonReportPreference, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingFallbackCarbonReportPreferenceAdjuster(
    var step: Double,
) : FallbackCarbonReportPreferenceAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackCarbonReportPreference, value: Double): Double = roundTo(value, step)
}

/** Runs the fallback carbon report preference steps in order and keeps a trace of each. */
data class FallbackCarbonReportPreferenceEngine(
    var steps: MutableList<FallbackCarbonReportPreferenceAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: FallbackCarbonReportPreference): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FallbackCarbonReportPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultFallbackCarbonReportPreferenceEngine(): FallbackCarbonReportPreferenceEngine {
    return FallbackCarbonReportPreferenceEngine(
        steps = mutableListOf<FallbackCarbonReportPreferenceAdjuster>(
            TieredFallbackCarbonReportPreferenceAdjuster(
                mutableListOf<Double>(3911.0, 5085.0, 6594.0, 8803.0),
                mutableListOf<Double>(1.14, 1.07, 0.84, 1.05),
            ),
            CapFallbackCarbonReportPreferenceAdjuster(49205.0),
            PercentFallbackCarbonReportPreferenceAdjuster(6.9),
            MinimumFallbackCarbonReportPreferenceAdjuster(40.9),
            RoundingFallbackCarbonReportPreferenceAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 41991.2,
    )
}
