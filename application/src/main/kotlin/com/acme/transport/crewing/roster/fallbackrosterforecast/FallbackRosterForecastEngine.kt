package com.acme.transport.crewing.roster.fallbackrosterforecast

/** One step of the fallback roster forecast calculation. */
interface FallbackRosterForecastAdjuster {
    fun describe(): String
    fun adjust(item: FallbackRosterForecast, value: Double): Double
}

data class TitleFallbackRosterForecastAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackRosterForecastAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: FallbackRosterForecast, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumFallbackRosterForecastAdjuster(var minimum: Double) : FallbackRosterForecastAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackRosterForecast, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentFallbackRosterForecastAdjuster(var percent: Double) : FallbackRosterForecastAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackRosterForecast, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredFallbackRosterForecastAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackRosterForecastAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackRosterForecast, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the fallback roster forecast steps in order and keeps a trace of each. */
data class FallbackRosterForecastEngine(
    var steps: MutableList<FallbackRosterForecastAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: FallbackRosterForecast): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackRosterForecastAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackRosterForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultFallbackRosterForecastEngine(): FallbackRosterForecastEngine {
    return FallbackRosterForecastEngine(
        steps = mutableListOf<FallbackRosterForecastAdjuster>(
            TitleFallbackRosterForecastAdjuster(mutableMapOf<String, Double>("central" to 1.39)),
            MinimumFallbackRosterForecastAdjuster(21.9),
            PercentFallbackRosterForecastAdjuster(21.6),
            TieredFallbackRosterForecastAdjuster(
                mutableListOf<Double>(485.0, 5161.0, 8573.0, 9175.0),
                mutableListOf<Double>(0.89, 1.02, 1.09, 1.12),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 95465.5,
    )
}
