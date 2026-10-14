package com.acme.transport.terminal.crossdock.cappeddockestimate

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the seasonal cross dock threshold calculation. */
interface SeasonalCrossDockThresholdAdjuster {
    fun describe(): String
    fun adjust(item: SeasonalCrossDockThreshold, value: Double): Double
}

data class DepotSeasonalCrossDockThresholdAdjuster(
    var factors: MutableMap<String, Double>,
) : SeasonalCrossDockThresholdAdjuster {
    override fun describe(): String = "by depot"

    override fun adjust(item: SeasonalCrossDockThreshold, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumSeasonalCrossDockThresholdAdjuster(var minimum: Double) : SeasonalCrossDockThresholdAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalCrossDockThreshold, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapSeasonalCrossDockThresholdAdjuster(var cap: Double) : SeasonalCrossDockThresholdAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalCrossDockThreshold, value: Double): Double = minOf(value, cap)
}

data class PercentSeasonalCrossDockThresholdAdjuster(var percent: Double) : SeasonalCrossDockThresholdAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalCrossDockThreshold, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the seasonal cross dock threshold steps in order and keeps a trace of each. */
data class SeasonalCrossDockThresholdEngine(
    var steps: MutableList<SeasonalCrossDockThresholdAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: SeasonalCrossDockThreshold): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SeasonalCrossDockThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultSeasonalCrossDockThresholdEngine(): SeasonalCrossDockThresholdEngine {
    return SeasonalCrossDockThresholdEngine(
        steps = mutableListOf<SeasonalCrossDockThresholdAdjuster>(
            DepotSeasonalCrossDockThresholdAdjuster(
                mutableMapOf<String, Double>("south" to 1.06, "west" to 1.15, "north" to 0.9),
            ),
            MinimumSeasonalCrossDockThresholdAdjuster(7.8),
            CapSeasonalCrossDockThresholdAdjuster(29074.0),
            PercentSeasonalCrossDockThresholdAdjuster(19.3),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 90673.6,
    )
}
