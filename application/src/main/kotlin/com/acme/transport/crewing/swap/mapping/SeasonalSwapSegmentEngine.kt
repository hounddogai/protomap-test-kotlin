package com.acme.transport.crewing.swap.mapping

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the seasonal swap segment calculation. */
interface SeasonalSwapSegmentStep {
    fun describe(): String
    fun adjust(item: SeasonalSwapSegment, value: Double): Double
}

data class MinimumSeasonalSwapSegmentAdjuster(var minimum: Double) : SeasonalSwapSegmentStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalSwapSegment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapSeasonalSwapSegmentAdjuster(var cap: Double) : SeasonalSwapSegmentStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalSwapSegment, value: Double): Double = minOf(value, cap)
}

data class NameSeasonalSwapSegmentAdjuster(var factors: MutableMap<String, Double>) : SeasonalSwapSegmentStep {
    override fun describe(): String = "by name"

    override fun adjust(item: SeasonalSwapSegment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingSeasonalSwapSegmentAdjuster(var step: Double) : SeasonalSwapSegmentStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalSwapSegment, value: Double): Double = roundTo(value, step)
}

data class TieredSeasonalSwapSegmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalSwapSegmentStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalSwapSegment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the seasonal swap segment steps in order and keeps a trace of each. */
data class SeasonalSwapSegmentEngine(
    var steps: MutableList<SeasonalSwapSegmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SeasonalSwapSegment): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SeasonalSwapSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSeasonalSwapSegmentEngine(): SeasonalSwapSegmentEngine {
    return SeasonalSwapSegmentEngine(
        steps = mutableListOf<SeasonalSwapSegmentStep>(
            MinimumSeasonalSwapSegmentAdjuster(43.6),
            CapSeasonalSwapSegmentAdjuster(1247.0),
            NameSeasonalSwapSegmentAdjuster(mutableMapOf<String, Double>("retail" to 0.75, "online" to 1.25)),
            RoundingSeasonalSwapSegmentAdjuster(0.1),
            TieredSeasonalSwapSegmentAdjuster(
                mutableListOf<Double>(3472.0, 5350.0, 8862.0),
                mutableListOf<Double>(0.89, 1.1, 1.04),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 23102.7,
    )
}
