package com.acme.transport.lastmile.attempt.threshold

import com.acme.transport.kit.flags.Registry

/** One step of the attempt checkpoint calculation. */
interface AttemptCheckpointAdjuster {
    fun describe(): String
    fun adjust(item: AttemptCheckpoint, value: Double): Double
}

data class AttemptCheckpointDiscountOffsetAdjuster(var weight: Double) : AttemptCheckpointAdjuster {
    override fun describe(): String = "plus discount times $weight"

    override fun adjust(item: AttemptCheckpoint, value: Double): Double = value + item.discount * weight
}

data class PercentAttemptCheckpointAdjuster(var percent: Double) : AttemptCheckpointAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AttemptCheckpoint, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedAttemptCheckpointAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AttemptCheckpointAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AttemptCheckpoint, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the attempt checkpoint steps in order and keeps a trace of each. */
data class AttemptCheckpointEngine(
    var steps: MutableList<AttemptCheckpointAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AttemptCheckpoint): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AttemptCheckpointAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<AttemptCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAttemptCheckpointEngine(flags: Registry): AttemptCheckpointEngine {
    return AttemptCheckpointEngine(
        steps = mutableListOf<AttemptCheckpointAdjuster>(
            AttemptCheckpointDiscountOffsetAdjuster(1.02),
            PercentAttemptCheckpointAdjuster(3.3),
            FlaggedAttemptCheckpointAdjuster(flags, "attempt-checkpoint-default", 1.2),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 17364.6,
    )
}
