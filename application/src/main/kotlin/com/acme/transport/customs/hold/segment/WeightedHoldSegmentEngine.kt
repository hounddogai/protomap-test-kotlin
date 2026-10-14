package com.acme.transport.customs.hold.segment

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the weighted hold segment calculation. */
interface WeightedHoldSegmentStep {
    fun describe(): String
    fun adjust(item: WeightedHoldSegment, value: Double): Double
}

data class LaneWeightedHoldSegmentAdjuster(var factors: MutableMap<String, Double>) : WeightedHoldSegmentStep {
    override fun describe(): String = "by lane"

    override fun adjust(item: WeightedHoldSegment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumWeightedHoldSegmentAdjuster(var minimum: Double) : WeightedHoldSegmentStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedHoldSegment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class WeightedHoldSegmentCostOffsetAdjuster(var weight: Double) : WeightedHoldSegmentStep {
    override fun describe(): String = "plus cost times $weight"

    override fun adjust(item: WeightedHoldSegment, value: Double): Double = value + item.cost * weight
}

/** Runs the weighted hold segment steps in order and keeps a trace of each. */
data class WeightedHoldSegmentEngine(
    var steps: MutableList<WeightedHoldSegmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: WeightedHoldSegment): Double {
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

    fun evaluateAll(items: MutableList<WeightedHoldSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultWeightedHoldSegmentEngine(): WeightedHoldSegmentEngine {
    return WeightedHoldSegmentEngine(
        steps = mutableListOf<WeightedHoldSegmentStep>(
            LaneWeightedHoldSegmentAdjuster(mutableMapOf<String, Double>("retail" to 0.87, "west" to 1.24)),
            MinimumWeightedHoldSegmentAdjuster(3.0),
            WeightedHoldSegmentCostOffsetAdjuster(1.97),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 75306.5,
    )
}
