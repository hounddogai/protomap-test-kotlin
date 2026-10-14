package com.acme.transport.emissions.carbonreport.reservation

/** One step of the fallback carbon report variance calculation. */
interface FallbackCarbonReportVarianceAdjuster {
    fun describe(): String
    fun adjust(item: FallbackCarbonReportVariance, value: Double): Double
}

data class MinimumFallbackCarbonReportVarianceAdjuster(
    var minimum: Double,
) : FallbackCarbonReportVarianceAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackCarbonReportVariance, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FallbackCarbonReportVarianceGraceDaysOffsetAdjuster(
    var weight: Double,
) : FallbackCarbonReportVarianceAdjuster {
    override fun describe(): String = "plus grace days times $weight"

    override fun adjust(item: FallbackCarbonReportVariance, value: Double): Double {
        return value + item.graceDays.toDouble() * weight
    }
}

data class PercentFallbackCarbonReportVarianceAdjuster(
    var percent: Double,
) : FallbackCarbonReportVarianceAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackCarbonReportVariance, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the fallback carbon report variance steps in order and keeps a trace of each. */
data class FallbackCarbonReportVarianceEngine(
    var steps: MutableList<FallbackCarbonReportVarianceAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: FallbackCarbonReportVariance): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackCarbonReportVarianceAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackCarbonReportVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultFallbackCarbonReportVarianceEngine(): FallbackCarbonReportVarianceEngine {
    return FallbackCarbonReportVarianceEngine(
        steps = mutableListOf<FallbackCarbonReportVarianceAdjuster>(
            MinimumFallbackCarbonReportVarianceAdjuster(23.9),
            FallbackCarbonReportVarianceGraceDaysOffsetAdjuster(0.46),
            PercentFallbackCarbonReportVarianceAdjuster(20.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 12145.9,
    )
}
