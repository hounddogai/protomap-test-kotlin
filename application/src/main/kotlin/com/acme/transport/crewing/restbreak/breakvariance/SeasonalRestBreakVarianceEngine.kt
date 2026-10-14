package com.acme.transport.crewing.restbreak.breakvariance

import com.acme.transport.kit.flags.Registry

/** One step of the seasonal rest break variance calculation. */
interface SeasonalRestBreakVarianceFactor {
    fun describe(): String
    fun adjust(item: SeasonalRestBreakVariance, value: Double): Double
}

data class TieredSeasonalRestBreakVarianceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalRestBreakVarianceFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalRestBreakVariance, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class TitleSeasonalRestBreakVarianceAdjuster(
    var factors: MutableMap<String, Double>,
) : SeasonalRestBreakVarianceFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: SeasonalRestBreakVariance, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumSeasonalRestBreakVarianceAdjuster(var minimum: Double) : SeasonalRestBreakVarianceFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalRestBreakVariance, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedSeasonalRestBreakVarianceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalRestBreakVarianceFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalRestBreakVariance, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentSeasonalRestBreakVarianceAdjuster(var percent: Double) : SeasonalRestBreakVarianceFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalRestBreakVariance, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the seasonal rest break variance steps in order and keeps a trace of each. */
data class SeasonalRestBreakVarianceEngine(
    var steps: MutableList<SeasonalRestBreakVarianceFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: SeasonalRestBreakVariance): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalRestBreakVarianceFactor = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalRestBreakVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultSeasonalRestBreakVarianceEngine(flags: Registry): SeasonalRestBreakVarianceEngine {
    return SeasonalRestBreakVarianceEngine(
        steps = mutableListOf<SeasonalRestBreakVarianceFactor>(
            TieredSeasonalRestBreakVarianceAdjuster(
                mutableListOf<Double>(3686.0, 3932.0, 4778.0, 6434.0),
                mutableListOf<Double>(0.94, 0.97, 0.94, 1.19),
            ),
            TitleSeasonalRestBreakVarianceAdjuster(mutableMapOf<String, Double>("online" to 1.08)),
            MinimumSeasonalRestBreakVarianceAdjuster(47.5),
            FlaggedSeasonalRestBreakVarianceAdjuster(flags, "seasonal-rest-break-variance-scheduled", 0.9),
            PercentSeasonalRestBreakVarianceAdjuster(19.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 84773.2,
    )
}
