package com.acme.transport.emissions.routeoffset.threshold

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the seasonal route offset threshold calculation. */
interface SeasonalRouteOffsetThresholdStep {
    fun describe(): String
    fun adjust(item: SeasonalRouteOffsetThreshold, value: Double): Double
}

data class CapSeasonalRouteOffsetThresholdAdjuster(var cap: Double) : SeasonalRouteOffsetThresholdStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalRouteOffsetThreshold, value: Double): Double = minOf(value, cap)
}

data class ChannelSeasonalRouteOffsetThresholdAdjuster(
    var factors: MutableMap<String, Double>,
) : SeasonalRouteOffsetThresholdStep {
    override fun describe(): String = "by channel"

    override fun adjust(item: SeasonalRouteOffsetThreshold, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingSeasonalRouteOffsetThresholdAdjuster(var step: Double) : SeasonalRouteOffsetThresholdStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalRouteOffsetThreshold, value: Double): Double = roundTo(value, step)
}

data class TieredSeasonalRouteOffsetThresholdAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalRouteOffsetThresholdStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalRouteOffsetThreshold, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class SeasonalRouteOffsetThresholdCapacityOffsetAdjuster(
    var weight: Double,
) : SeasonalRouteOffsetThresholdStep {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: SeasonalRouteOffsetThreshold, value: Double): Double {
        return value + item.capacity.toDouble() * weight
    }
}

data class MinimumSeasonalRouteOffsetThresholdAdjuster(var minimum: Double) : SeasonalRouteOffsetThresholdStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalRouteOffsetThreshold, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the seasonal route offset threshold steps in order and keeps a trace of each. */
data class SeasonalRouteOffsetThresholdEngine(
    var steps: MutableList<SeasonalRouteOffsetThresholdStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SeasonalRouteOffsetThreshold): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SeasonalRouteOffsetThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSeasonalRouteOffsetThresholdEngine(): SeasonalRouteOffsetThresholdEngine {
    return SeasonalRouteOffsetThresholdEngine(
        steps = mutableListOf<SeasonalRouteOffsetThresholdStep>(
            CapSeasonalRouteOffsetThresholdAdjuster(26051.0),
            ChannelSeasonalRouteOffsetThresholdAdjuster(mutableMapOf<String, Double>("north" to 1.24)),
            RoundingSeasonalRouteOffsetThresholdAdjuster(0.25),
            TieredSeasonalRouteOffsetThresholdAdjuster(
                mutableListOf<Double>(5311.0, 8716.0, 9819.0),
                mutableListOf<Double>(0.97, 1.06, 0.88),
            ),
            SeasonalRouteOffsetThresholdCapacityOffsetAdjuster(0.51),
            MinimumSeasonalRouteOffsetThresholdAdjuster(27.7),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 80168.2,
    )
}
