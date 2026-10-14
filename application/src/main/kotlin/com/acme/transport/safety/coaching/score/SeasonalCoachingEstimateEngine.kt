package com.acme.transport.safety.coaching.score

import com.acme.transport.kit.numbers.roundTo

/** One step of the seasonal coaching estimate calculation. */
interface SeasonalCoachingEstimateModifier {
    fun describe(): String
    fun adjust(item: SeasonalCoachingEstimate, value: Double): Double
}

data class PercentSeasonalCoachingEstimateAdjuster(var percent: Double) : SeasonalCoachingEstimateModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalCoachingEstimate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredSeasonalCoachingEstimateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalCoachingEstimateModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalCoachingEstimate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumSeasonalCoachingEstimateAdjuster(var minimum: Double) : SeasonalCoachingEstimateModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalCoachingEstimate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingSeasonalCoachingEstimateAdjuster(var step: Double) : SeasonalCoachingEstimateModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalCoachingEstimate, value: Double): Double = roundTo(value, step)
}

/** Runs the seasonal coaching estimate steps in order and keeps a trace of each. */
data class SeasonalCoachingEstimateEngine(
    var steps: MutableList<SeasonalCoachingEstimateModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: SeasonalCoachingEstimate): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalCoachingEstimateModifier = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalCoachingEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultSeasonalCoachingEstimateEngine(): SeasonalCoachingEstimateEngine {
    return SeasonalCoachingEstimateEngine(
        steps = mutableListOf<SeasonalCoachingEstimateModifier>(
            PercentSeasonalCoachingEstimateAdjuster(12.1),
            TieredSeasonalCoachingEstimateAdjuster(
                mutableListOf<Double>(1361.0, 3896.0),
                mutableListOf<Double>(0.83, 0.9),
            ),
            MinimumSeasonalCoachingEstimateAdjuster(13.4),
            RoundingSeasonalCoachingEstimateAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 67838.4,
    )
}
