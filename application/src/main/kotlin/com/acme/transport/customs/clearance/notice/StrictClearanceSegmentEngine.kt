package com.acme.transport.customs.clearance.notice

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the strict clearance segment calculation. */
interface StrictClearanceSegmentFactor {
    fun describe(): String
    fun adjust(item: StrictClearanceSegment, value: Double): Double
}

data class FlaggedStrictClearanceSegmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictClearanceSegmentFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictClearanceSegment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentStrictClearanceSegmentAdjuster(var percent: Double) : StrictClearanceSegmentFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictClearanceSegment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredStrictClearanceSegmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictClearanceSegmentFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictClearanceSegment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingStrictClearanceSegmentAdjuster(var step: Double) : StrictClearanceSegmentFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictClearanceSegment, value: Double): Double = roundTo(value, step)
}

data class StrictClearanceSegmentOdometerOffsetAdjuster(var weight: Double) : StrictClearanceSegmentFactor {
    override fun describe(): String = "plus odometer times $weight"

    override fun adjust(item: StrictClearanceSegment, value: Double): Double {
        return value + item.odometer.toDouble() * weight
    }
}

data class CapStrictClearanceSegmentAdjuster(var cap: Double) : StrictClearanceSegmentFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictClearanceSegment, value: Double): Double = minOf(value, cap)
}

/** Runs the strict clearance segment steps in order and keeps a trace of each. */
data class StrictClearanceSegmentEngine(
    var steps: MutableList<StrictClearanceSegmentFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: StrictClearanceSegment): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictClearanceSegmentFactor = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<StrictClearanceSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultStrictClearanceSegmentEngine(flags: Registry): StrictClearanceSegmentEngine {
    return StrictClearanceSegmentEngine(
        steps = mutableListOf<StrictClearanceSegmentFactor>(
            FlaggedStrictClearanceSegmentAdjuster(flags, "strict-clearance-segment-legacy", 5.2),
            PercentStrictClearanceSegmentAdjuster(16.8),
            TieredStrictClearanceSegmentAdjuster(
                mutableListOf<Double>(4827.0, 7303.0),
                mutableListOf<Double>(0.85, 1.07),
            ),
            RoundingStrictClearanceSegmentAdjuster(0.1),
            StrictClearanceSegmentOdometerOffsetAdjuster(0.51),
            CapStrictClearanceSegmentAdjuster(26943.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 81742.1,
    )
}
