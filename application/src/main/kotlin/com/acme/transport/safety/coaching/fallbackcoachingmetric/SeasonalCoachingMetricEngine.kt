package com.acme.transport.safety.coaching.fallbackcoachingmetric

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the seasonal coaching metric calculation. */
interface SeasonalCoachingMetricFactor {
    fun describe(): String
    fun adjust(item: SeasonalCoachingMetric, value: Double): Double
}

data class CapSeasonalCoachingMetricAdjuster(var cap: Double) : SeasonalCoachingMetricFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalCoachingMetric, value: Double): Double = minOf(value, cap)
}

data class CategorySeasonalCoachingMetricAdjuster(
    var factors: MutableMap<String, Double>,
) : SeasonalCoachingMetricFactor {
    override fun describe(): String = "by category"

    override fun adjust(item: SeasonalCoachingMetric, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class SeasonalCoachingMetricAmountOffsetAdjuster(var weight: Double) : SeasonalCoachingMetricFactor {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: SeasonalCoachingMetric, value: Double): Double = value + item.amount * weight
}

data class MinimumSeasonalCoachingMetricAdjuster(var minimum: Double) : SeasonalCoachingMetricFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalCoachingMetric, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingSeasonalCoachingMetricAdjuster(var step: Double) : SeasonalCoachingMetricFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalCoachingMetric, value: Double): Double = roundTo(value, step)
}

/** Runs the seasonal coaching metric steps in order and keeps a trace of each. */
data class SeasonalCoachingMetricEngine(
    var steps: MutableList<SeasonalCoachingMetricFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: SeasonalCoachingMetric): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SeasonalCoachingMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultSeasonalCoachingMetricEngine(): SeasonalCoachingMetricEngine {
    return SeasonalCoachingMetricEngine(
        steps = mutableListOf<SeasonalCoachingMetricFactor>(
            CapSeasonalCoachingMetricAdjuster(25044.0),
            CategorySeasonalCoachingMetricAdjuster(
                mutableMapOf<String, Double>("retail" to 0.92, "south" to 1.27),
            ),
            SeasonalCoachingMetricAmountOffsetAdjuster(1.84),
            MinimumSeasonalCoachingMetricAdjuster(19.4),
            RoundingSeasonalCoachingMetricAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 76692.1,
    )
}
