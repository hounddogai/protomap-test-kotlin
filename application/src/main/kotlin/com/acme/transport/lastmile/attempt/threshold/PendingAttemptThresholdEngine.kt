package com.acme.transport.lastmile.attempt.threshold

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the pending attempt threshold calculation. */
interface PendingAttemptThresholdFactor {
    fun describe(): String
    fun adjust(item: PendingAttemptThreshold, value: Double): Double
}

data class MinimumPendingAttemptThresholdAdjuster(var minimum: Double) : PendingAttemptThresholdFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingAttemptThreshold, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PendingAttemptThresholdBatchSizeOffsetAdjuster(var weight: Double) : PendingAttemptThresholdFactor {
    override fun describe(): String = "plus batch size times $weight"

    override fun adjust(item: PendingAttemptThreshold, value: Double): Double {
        return value + item.batchSize.toDouble() * weight
    }
}

data class RoundingPendingAttemptThresholdAdjuster(var step: Double) : PendingAttemptThresholdFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingAttemptThreshold, value: Double): Double = roundTo(value, step)
}

data class FlaggedPendingAttemptThresholdAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingAttemptThresholdFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingAttemptThreshold, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentPendingAttemptThresholdAdjuster(var percent: Double) : PendingAttemptThresholdFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingAttemptThreshold, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredPendingAttemptThresholdAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingAttemptThresholdFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingAttemptThreshold, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the pending attempt threshold steps in order and keeps a trace of each. */
data class PendingAttemptThresholdEngine(
    var steps: MutableList<PendingAttemptThresholdFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PendingAttemptThreshold): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingAttemptThresholdFactor = steps[index]
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

    fun evaluateAll(items: MutableList<PendingAttemptThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPendingAttemptThresholdEngine(flags: Registry): PendingAttemptThresholdEngine {
    return PendingAttemptThresholdEngine(
        steps = mutableListOf<PendingAttemptThresholdFactor>(
            MinimumPendingAttemptThresholdAdjuster(40.5),
            PendingAttemptThresholdBatchSizeOffsetAdjuster(0.21),
            RoundingPendingAttemptThresholdAdjuster(0.25),
            FlaggedPendingAttemptThresholdAdjuster(flags, "pending-attempt-threshold-fallback", -9.2),
            PercentPendingAttemptThresholdAdjuster(-17.4),
            TieredPendingAttemptThresholdAdjuster(
                mutableListOf<Double>(501.0, 2131.0, 2261.0, 9123.0),
                mutableListOf<Double>(1.13, 1.14, 1.2, 0.98),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 91694.7,
    )
}
