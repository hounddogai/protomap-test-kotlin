package com.acme.transport.lastmile.attempt.window

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the capped attempt segment calculation. */
interface CappedAttemptSegmentFactor {
    fun describe(): String
    fun adjust(item: CappedAttemptSegment, value: Double): Double
}

data class CapCappedAttemptSegmentAdjuster(var cap: Double) : CappedAttemptSegmentFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedAttemptSegment, value: Double): Double = minOf(value, cap)
}

data class TieredCappedAttemptSegmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedAttemptSegmentFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedAttemptSegment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingCappedAttemptSegmentAdjuster(var step: Double) : CappedAttemptSegmentFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedAttemptSegment, value: Double): Double = roundTo(value, step)
}

data class FlaggedCappedAttemptSegmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedAttemptSegmentFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedAttemptSegment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the capped attempt segment steps in order and keeps a trace of each. */
data class CappedAttemptSegmentEngine(
    var steps: MutableList<CappedAttemptSegmentFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CappedAttemptSegment): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedAttemptSegmentFactor = steps[index]
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

    fun evaluateAll(items: MutableList<CappedAttemptSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCappedAttemptSegmentEngine(flags: Registry): CappedAttemptSegmentEngine {
    return CappedAttemptSegmentEngine(
        steps = mutableListOf<CappedAttemptSegmentFactor>(
            CapCappedAttemptSegmentAdjuster(25312.0),
            TieredCappedAttemptSegmentAdjuster(
                mutableListOf<Double>(6641.0, 6802.0, 8199.0, 8320.0),
                mutableListOf<Double>(0.82, 0.8, 1.12, 1.15),
            ),
            RoundingCappedAttemptSegmentAdjuster(0.25),
            FlaggedCappedAttemptSegmentAdjuster(flags, "capped-attempt-segment-regional", -1.7),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 41176.1,
    )
}
