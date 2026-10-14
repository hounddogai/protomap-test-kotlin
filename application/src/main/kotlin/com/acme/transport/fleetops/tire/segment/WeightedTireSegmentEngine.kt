package com.acme.transport.fleetops.tire.segment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the weighted tire segment calculation. */
interface WeightedTireSegmentFactor {
    fun describe(): String
    fun adjust(item: WeightedTireSegment, value: Double): Double
}

data class NameWeightedTireSegmentAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedTireSegmentFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: WeightedTireSegment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingWeightedTireSegmentAdjuster(var step: Double) : WeightedTireSegmentFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedTireSegment, value: Double): Double = roundTo(value, step)
}

data class TieredWeightedTireSegmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedTireSegmentFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedTireSegment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedWeightedTireSegmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedTireSegmentFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedTireSegment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentWeightedTireSegmentAdjuster(var percent: Double) : WeightedTireSegmentFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedTireSegment, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the weighted tire segment steps in order and keeps a trace of each. */
data class WeightedTireSegmentEngine(
    var steps: MutableList<WeightedTireSegmentFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: WeightedTireSegment): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<WeightedTireSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultWeightedTireSegmentEngine(flags: Registry): WeightedTireSegmentEngine {
    return WeightedTireSegmentEngine(
        steps = mutableListOf<WeightedTireSegmentFactor>(
            NameWeightedTireSegmentAdjuster(
                mutableMapOf<String, Double>("south" to 0.8, "north" to 1.15, "retail" to 1.19),
            ),
            RoundingWeightedTireSegmentAdjuster(1.0),
            TieredWeightedTireSegmentAdjuster(
                mutableListOf<Double>(76.0, 3351.0, 4250.0),
                mutableListOf<Double>(1.14, 1.11, 1.05),
            ),
            FlaggedWeightedTireSegmentAdjuster(flags, "weighted-tire-segment-default", -3.5),
            PercentWeightedTireSegmentAdjuster(-23.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 32139.8,
    )
}
