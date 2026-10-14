package com.acme.transport.lastmile.redelivery.segment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the redelivery segment calculation. */
interface RedeliverySegmentAdjuster {
    fun describe(): String
    fun adjust(item: RedeliverySegment, value: Double): Double
}

data class PercentRedeliverySegmentAdjuster(var percent: Double) : RedeliverySegmentAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RedeliverySegment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingRedeliverySegmentAdjuster(var step: Double) : RedeliverySegmentAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RedeliverySegment, value: Double): Double = roundTo(value, step)
}

data class FlaggedRedeliverySegmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RedeliverySegmentAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RedeliverySegment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RedeliverySegmentEndsAtOffsetAdjuster(var weight: Double) : RedeliverySegmentAdjuster {
    override fun describe(): String = "plus ends at times $weight"

    override fun adjust(item: RedeliverySegment, value: Double): Double = value + item.endsAt.toDouble() * weight
}

/** Runs the redelivery segment steps in order and keeps a trace of each. */
data class RedeliverySegmentEngine(
    var steps: MutableList<RedeliverySegmentAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RedeliverySegment): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RedeliverySegmentAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RedeliverySegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRedeliverySegmentEngine(flags: Registry): RedeliverySegmentEngine {
    return RedeliverySegmentEngine(
        steps = mutableListOf<RedeliverySegmentAdjuster>(
            PercentRedeliverySegmentAdjuster(-10.5),
            RoundingRedeliverySegmentAdjuster(5.0),
            FlaggedRedeliverySegmentAdjuster(flags, "redelivery-segment-seasonal", 0.4),
            RedeliverySegmentEndsAtOffsetAdjuster(1.36),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 75555.2,
    )
}
