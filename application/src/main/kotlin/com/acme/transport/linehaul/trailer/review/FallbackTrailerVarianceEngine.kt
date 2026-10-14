package com.acme.transport.linehaul.trailer.review

/** One step of the fallback trailer variance calculation. */
interface FallbackTrailerVarianceAdjuster {
    fun describe(): String
    fun adjust(item: FallbackTrailerVariance, value: Double): Double
}

data class MinimumFallbackTrailerVarianceAdjuster(var minimum: Double) : FallbackTrailerVarianceAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackTrailerVariance, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentFallbackTrailerVarianceAdjuster(var percent: Double) : FallbackTrailerVarianceAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackTrailerVariance, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapFallbackTrailerVarianceAdjuster(var cap: Double) : FallbackTrailerVarianceAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackTrailerVariance, value: Double): Double = minOf(value, cap)
}

/** Runs the fallback trailer variance steps in order and keeps a trace of each. */
data class FallbackTrailerVarianceEngine(
    var steps: MutableList<FallbackTrailerVarianceAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: FallbackTrailerVariance): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackTrailerVarianceAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackTrailerVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultFallbackTrailerVarianceEngine(): FallbackTrailerVarianceEngine {
    return FallbackTrailerVarianceEngine(
        steps = mutableListOf<FallbackTrailerVarianceAdjuster>(
            MinimumFallbackTrailerVarianceAdjuster(13.4),
            PercentFallbackTrailerVarianceAdjuster(18.7),
            CapFallbackTrailerVarianceAdjuster(25771.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 92647.8,
    )
}
