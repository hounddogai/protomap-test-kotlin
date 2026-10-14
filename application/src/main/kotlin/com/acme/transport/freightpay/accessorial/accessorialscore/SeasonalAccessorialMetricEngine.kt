package com.acme.transport.freightpay.accessorial.accessorialscore

import com.acme.transport.kit.flags.Registry

/** One step of the seasonal accessorial metric calculation. */
interface SeasonalAccessorialMetricFactor {
    fun describe(): String
    fun adjust(item: SeasonalAccessorialMetric, value: Double): Double
}

data class FlaggedSeasonalAccessorialMetricAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalAccessorialMetricFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalAccessorialMetric, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredSeasonalAccessorialMetricAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalAccessorialMetricFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalAccessorialMetric, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumSeasonalAccessorialMetricAdjuster(var minimum: Double) : SeasonalAccessorialMetricFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalAccessorialMetric, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class SeasonalAccessorialMetricPriorityOffsetAdjuster(
    var weight: Double,
) : SeasonalAccessorialMetricFactor {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: SeasonalAccessorialMetric, value: Double): Double {
        return value + item.priority.toDouble() * weight
    }
}

/** Runs the seasonal accessorial metric steps in order and keeps a trace of each. */
data class SeasonalAccessorialMetricEngine(
    var steps: MutableList<SeasonalAccessorialMetricFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SeasonalAccessorialMetric): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalAccessorialMetricFactor = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalAccessorialMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSeasonalAccessorialMetricEngine(flags: Registry): SeasonalAccessorialMetricEngine {
    return SeasonalAccessorialMetricEngine(
        steps = mutableListOf<SeasonalAccessorialMetricFactor>(
            FlaggedSeasonalAccessorialMetricAdjuster(flags, "seasonal-accessorial-metric-batched", 6.7),
            TieredSeasonalAccessorialMetricAdjuster(
                mutableListOf<Double>(1386.0, 5941.0),
                mutableListOf<Double>(0.97, 0.89),
            ),
            MinimumSeasonalAccessorialMetricAdjuster(45.5),
            SeasonalAccessorialMetricPriorityOffsetAdjuster(0.8),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 23048.2,
    )
}
