package com.acme.transport.safety.coaching.coachingsegment

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the seasonal coaching variance calculation. */
interface SeasonalCoachingVarianceFactor {
    fun describe(): String
    fun adjust(item: SeasonalCoachingVariance, value: Double): Double
}

data class MinimumSeasonalCoachingVarianceAdjuster(var minimum: Double) : SeasonalCoachingVarianceFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalCoachingVariance, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapSeasonalCoachingVarianceAdjuster(var cap: Double) : SeasonalCoachingVarianceFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalCoachingVariance, value: Double): Double = minOf(value, cap)
}

data class SeasonalCoachingVarianceThresholdOffsetAdjuster(
    var weight: Double,
) : SeasonalCoachingVarianceFactor {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: SeasonalCoachingVariance, value: Double): Double = value + item.threshold * weight
}

data class PercentSeasonalCoachingVarianceAdjuster(var percent: Double) : SeasonalCoachingVarianceFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalCoachingVariance, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the seasonal coaching variance steps in order and keeps a trace of each. */
data class SeasonalCoachingVarianceEngine(
    var steps: MutableList<SeasonalCoachingVarianceFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: SeasonalCoachingVariance): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SeasonalCoachingVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultSeasonalCoachingVarianceEngine(): SeasonalCoachingVarianceEngine {
    return SeasonalCoachingVarianceEngine(
        steps = mutableListOf<SeasonalCoachingVarianceFactor>(
            MinimumSeasonalCoachingVarianceAdjuster(7.0),
            CapSeasonalCoachingVarianceAdjuster(14635.0),
            SeasonalCoachingVarianceThresholdOffsetAdjuster(1.32),
            PercentSeasonalCoachingVarianceAdjuster(8.7),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 28324.7,
    )
}
