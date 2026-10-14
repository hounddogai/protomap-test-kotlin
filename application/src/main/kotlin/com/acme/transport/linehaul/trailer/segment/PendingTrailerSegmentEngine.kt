package com.acme.transport.linehaul.trailer.segment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the pending trailer segment calculation. */
interface PendingTrailerSegmentStep {
    fun describe(): String
    fun adjust(item: PendingTrailerSegment, value: Double): Double
}

data class LabelPendingTrailerSegmentAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingTrailerSegmentStep {
    override fun describe(): String = "by label"

    override fun adjust(item: PendingTrailerSegment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingPendingTrailerSegmentAdjuster(var step: Double) : PendingTrailerSegmentStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingTrailerSegment, value: Double): Double = roundTo(value, step)
}

data class CapPendingTrailerSegmentAdjuster(var cap: Double) : PendingTrailerSegmentStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingTrailerSegment, value: Double): Double = minOf(value, cap)
}

data class FlaggedPendingTrailerSegmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingTrailerSegmentStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingTrailerSegment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the pending trailer segment steps in order and keeps a trace of each. */
data class PendingTrailerSegmentEngine(
    var steps: MutableList<PendingTrailerSegmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PendingTrailerSegment): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingTrailerSegmentStep = steps[index]
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

    fun evaluateAll(items: MutableList<PendingTrailerSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPendingTrailerSegmentEngine(flags: Registry): PendingTrailerSegmentEngine {
    return PendingTrailerSegmentEngine(
        steps = mutableListOf<PendingTrailerSegmentStep>(
            LabelPendingTrailerSegmentAdjuster(mutableMapOf<String, Double>("online" to 0.83, "west" to 1.04)),
            RoundingPendingTrailerSegmentAdjuster(5.0),
            CapPendingTrailerSegmentAdjuster(47205.0),
            FlaggedPendingTrailerSegmentAdjuster(flags, "pending-trailer-segment-adaptive", -5.7),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 93327.3,
    )
}
