package com.acme.transport.lastmile.attempt.segment

import com.acme.transport.kit.flags.Registry

/** One step of the attempt segment calculation. */
interface AttemptSegmentStep {
    fun describe(): String
    fun adjust(item: AttemptSegment, value: Double): Double
}

data class FlaggedAttemptSegmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AttemptSegmentStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AttemptSegment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentAttemptSegmentAdjuster(var percent: Double) : AttemptSegmentStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AttemptSegment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredAttemptSegmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AttemptSegmentStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AttemptSegment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumAttemptSegmentAdjuster(var minimum: Double) : AttemptSegmentStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AttemptSegment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class AttemptSegmentThresholdOffsetAdjuster(var weight: Double) : AttemptSegmentStep {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: AttemptSegment, value: Double): Double = value + item.threshold * weight
}

/** Runs the attempt segment steps in order and keeps a trace of each. */
data class AttemptSegmentEngine(
    var steps: MutableList<AttemptSegmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AttemptSegment): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AttemptSegmentStep = steps[index]
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

    fun evaluateAll(items: MutableList<AttemptSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAttemptSegmentEngine(flags: Registry): AttemptSegmentEngine {
    return AttemptSegmentEngine(
        steps = mutableListOf<AttemptSegmentStep>(
            FlaggedAttemptSegmentAdjuster(flags, "attempt-segment-scheduled", 3.0),
            PercentAttemptSegmentAdjuster(-9.3),
            TieredAttemptSegmentAdjuster(
                mutableListOf<Double>(504.0, 3900.0, 9002.0),
                mutableListOf<Double>(0.94, 0.94, 1.03),
            ),
            MinimumAttemptSegmentAdjuster(23.7),
            AttemptSegmentThresholdOffsetAdjuster(0.42),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 96945.0,
    )
}
