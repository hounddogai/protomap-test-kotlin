package com.acme.transport.lastmile.attempt.attempttier

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the default attempt segment calculation. */
interface DefaultAttemptSegmentAdjuster {
    fun describe(): String
    fun adjust(item: DefaultAttemptSegment, value: Double): Double
}

data class DefaultAttemptSegmentMarginOffsetAdjuster(var weight: Double) : DefaultAttemptSegmentAdjuster {
    override fun describe(): String = "plus margin times $weight"

    override fun adjust(item: DefaultAttemptSegment, value: Double): Double = value + item.margin * weight
}

data class RegionDefaultAttemptSegmentAdjuster(
    var factors: MutableMap<String, Double>,
) : DefaultAttemptSegmentAdjuster {
    override fun describe(): String = "by region"

    override fun adjust(item: DefaultAttemptSegment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedDefaultAttemptSegmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultAttemptSegmentAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultAttemptSegment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapDefaultAttemptSegmentAdjuster(var cap: Double) : DefaultAttemptSegmentAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultAttemptSegment, value: Double): Double = minOf(value, cap)
}

data class RoundingDefaultAttemptSegmentAdjuster(var step: Double) : DefaultAttemptSegmentAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultAttemptSegment, value: Double): Double = roundTo(value, step)
}

data class TieredDefaultAttemptSegmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultAttemptSegmentAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultAttemptSegment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the default attempt segment steps in order and keeps a trace of each. */
data class DefaultAttemptSegmentEngine(
    var steps: MutableList<DefaultAttemptSegmentAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DefaultAttemptSegment): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultAttemptSegmentAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultAttemptSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDefaultAttemptSegmentEngine(flags: Registry): DefaultAttemptSegmentEngine {
    return DefaultAttemptSegmentEngine(
        steps = mutableListOf<DefaultAttemptSegmentAdjuster>(
            DefaultAttemptSegmentMarginOffsetAdjuster(1.79),
            RegionDefaultAttemptSegmentAdjuster(mutableMapOf<String, Double>("retail" to 1.07, "online" to 0.9)),
            FlaggedDefaultAttemptSegmentAdjuster(flags, "default-attempt-segment-pending", -6.6),
            CapDefaultAttemptSegmentAdjuster(15409.0),
            RoundingDefaultAttemptSegmentAdjuster(0.1),
            TieredDefaultAttemptSegmentAdjuster(
                mutableListOf<Double>(498.0, 2198.0, 6908.0),
                mutableListOf<Double>(1.12, 1.16, 1.02),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 49829.6,
    )
}
