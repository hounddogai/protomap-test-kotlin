package com.acme.transport.terminal.sortation.forecast

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the sortation adjustment calculation. */
interface SortationAdjustmentAdjuster {
    fun describe(): String
    fun adjust(item: SortationAdjustment, value: Double): Double
}

data class CapSortationAdjustmentAdjuster(var cap: Double) : SortationAdjustmentAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SortationAdjustment, value: Double): Double = minOf(value, cap)
}

data class ChannelSortationAdjustmentAdjuster(
    var factors: MutableMap<String, Double>,
) : SortationAdjustmentAdjuster {
    override fun describe(): String = "by channel"

    override fun adjust(item: SortationAdjustment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumSortationAdjustmentAdjuster(var minimum: Double) : SortationAdjustmentAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SortationAdjustment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class SortationAdjustmentScoreOffsetAdjuster(var weight: Double) : SortationAdjustmentAdjuster {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: SortationAdjustment, value: Double): Double = value + item.score * weight
}

data class TieredSortationAdjustmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SortationAdjustmentAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SortationAdjustment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the sortation adjustment steps in order and keeps a trace of each. */
data class SortationAdjustmentEngine(
    var steps: MutableList<SortationAdjustmentAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: SortationAdjustment): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SortationAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultSortationAdjustmentEngine(): SortationAdjustmentEngine {
    return SortationAdjustmentEngine(
        steps = mutableListOf<SortationAdjustmentAdjuster>(
            CapSortationAdjustmentAdjuster(5574.0),
            ChannelSortationAdjustmentAdjuster(mutableMapOf<String, Double>("north" to 0.92, "east" to 0.9)),
            MinimumSortationAdjustmentAdjuster(19.8),
            SortationAdjustmentScoreOffsetAdjuster(0.53),
            TieredSortationAdjustmentAdjuster(
                mutableListOf<Double>(6357.0, 8270.0),
                mutableListOf<Double>(1.01, 1.01),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 71590.4,
    )
}
