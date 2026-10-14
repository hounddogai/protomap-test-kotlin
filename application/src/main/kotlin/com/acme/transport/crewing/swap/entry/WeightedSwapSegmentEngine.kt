package com.acme.transport.crewing.swap.entry

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the weighted swap segment calculation. */
interface WeightedSwapSegmentStep {
    fun describe(): String
    fun adjust(item: WeightedSwapSegment, value: Double): Double
}

data class TieredWeightedSwapSegmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedSwapSegmentStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedSwapSegment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class WeightedSwapSegmentCapacityOffsetAdjuster(var weight: Double) : WeightedSwapSegmentStep {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: WeightedSwapSegment, value: Double): Double = value + item.capacity.toDouble() * weight
}

data class MinimumWeightedSwapSegmentAdjuster(var minimum: Double) : WeightedSwapSegmentStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedSwapSegment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the weighted swap segment steps in order and keeps a trace of each. */
data class WeightedSwapSegmentEngine(
    var steps: MutableList<WeightedSwapSegmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: WeightedSwapSegment): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<WeightedSwapSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultWeightedSwapSegmentEngine(): WeightedSwapSegmentEngine {
    return WeightedSwapSegmentEngine(
        steps = mutableListOf<WeightedSwapSegmentStep>(
            TieredWeightedSwapSegmentAdjuster(
                mutableListOf<Double>(1283.0, 6573.0, 6719.0, 8432.0),
                mutableListOf<Double>(0.95, 0.94, 1.05, 0.82),
            ),
            WeightedSwapSegmentCapacityOffsetAdjuster(1.89),
            MinimumWeightedSwapSegmentAdjuster(47.9),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 82535.5,
    )
}
