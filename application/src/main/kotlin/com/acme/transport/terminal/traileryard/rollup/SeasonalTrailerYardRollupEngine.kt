package com.acme.transport.terminal.traileryard.rollup

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the seasonal trailer yard rollup calculation. */
interface SeasonalTrailerYardRollupAdjuster {
    fun describe(): String
    fun adjust(item: SeasonalTrailerYardRollup, value: Double): Double
}

data class MinimumSeasonalTrailerYardRollupAdjuster(var minimum: Double) : SeasonalTrailerYardRollupAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalTrailerYardRollup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredSeasonalTrailerYardRollupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalTrailerYardRollupAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalTrailerYardRollup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LabelSeasonalTrailerYardRollupAdjuster(
    var factors: MutableMap<String, Double>,
) : SeasonalTrailerYardRollupAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: SeasonalTrailerYardRollup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingSeasonalTrailerYardRollupAdjuster(var step: Double) : SeasonalTrailerYardRollupAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalTrailerYardRollup, value: Double): Double = roundTo(value, step)
}

/** Runs the seasonal trailer yard rollup steps in order and keeps a trace of each. */
data class SeasonalTrailerYardRollupEngine(
    var steps: MutableList<SeasonalTrailerYardRollupAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SeasonalTrailerYardRollup): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SeasonalTrailerYardRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSeasonalTrailerYardRollupEngine(): SeasonalTrailerYardRollupEngine {
    return SeasonalTrailerYardRollupEngine(
        steps = mutableListOf<SeasonalTrailerYardRollupAdjuster>(
            MinimumSeasonalTrailerYardRollupAdjuster(30.8),
            TieredSeasonalTrailerYardRollupAdjuster(
                mutableListOf<Double>(7192.0, 7216.0, 9036.0),
                mutableListOf<Double>(0.97, 1.13, 0.97),
            ),
            LabelSeasonalTrailerYardRollupAdjuster(
                mutableMapOf<String, Double>("west" to 1.13, "south" to 1.12, "central" to 1.06),
            ),
            RoundingSeasonalTrailerYardRollupAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 85818.5,
    )
}
