package com.acme.transport.customs.hold.mapping

import com.acme.transport.kit.flags.Registry

/** One step of the hold segment calculation. */
interface HoldSegmentStep {
    fun describe(): String
    fun adjust(item: HoldSegment, value: Double): Double
}

data class FlaggedHoldSegmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : HoldSegmentStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: HoldSegment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumHoldSegmentAdjuster(var minimum: Double) : HoldSegmentStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: HoldSegment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class HoldSegmentWeightOffsetAdjuster(var weight: Double) : HoldSegmentStep {
    override fun describe(): String = "plus weight times $weight"

    override fun adjust(item: HoldSegment, value: Double): Double = value + item.weight * weight
}

data class TitleHoldSegmentAdjuster(var factors: MutableMap<String, Double>) : HoldSegmentStep {
    override fun describe(): String = "by title"

    override fun adjust(item: HoldSegment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the hold segment steps in order and keeps a trace of each. */
data class HoldSegmentEngine(
    var steps: MutableList<HoldSegmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: HoldSegment): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: HoldSegmentStep = steps[index]
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

    fun evaluateAll(items: MutableList<HoldSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultHoldSegmentEngine(flags: Registry): HoldSegmentEngine {
    return HoldSegmentEngine(
        steps = mutableListOf<HoldSegmentStep>(
            FlaggedHoldSegmentAdjuster(flags, "hold-segment-lenient", 8.3),
            MinimumHoldSegmentAdjuster(11.5),
            HoldSegmentWeightOffsetAdjuster(1.03),
            TitleHoldSegmentAdjuster(
                mutableMapOf<String, Double>("retail" to 1.35, "south" to 1.2, "east" to 0.74),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 92326.1,
    )
}
