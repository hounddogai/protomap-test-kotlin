package com.acme.transport.linehaul.trailer.trailerbatch

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the batched trailer profile calculation. */
interface BatchedTrailerProfileFactor {
    fun describe(): String
    fun adjust(item: BatchedTrailerProfile, value: Double): Double
}

data class RoundingBatchedTrailerProfileAdjuster(var step: Double) : BatchedTrailerProfileFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedTrailerProfile, value: Double): Double = roundTo(value, step)
}

data class CapBatchedTrailerProfileAdjuster(var cap: Double) : BatchedTrailerProfileFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedTrailerProfile, value: Double): Double = minOf(value, cap)
}

data class FlaggedBatchedTrailerProfileAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedTrailerProfileFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedTrailerProfile, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumBatchedTrailerProfileAdjuster(var minimum: Double) : BatchedTrailerProfileFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedTrailerProfile, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentBatchedTrailerProfileAdjuster(var percent: Double) : BatchedTrailerProfileFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedTrailerProfile, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the batched trailer profile steps in order and keeps a trace of each. */
data class BatchedTrailerProfileEngine(
    var steps: MutableList<BatchedTrailerProfileFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BatchedTrailerProfile): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedTrailerProfileFactor = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedTrailerProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBatchedTrailerProfileEngine(flags: Registry): BatchedTrailerProfileEngine {
    return BatchedTrailerProfileEngine(
        steps = mutableListOf<BatchedTrailerProfileFactor>(
            RoundingBatchedTrailerProfileAdjuster(5.0),
            CapBatchedTrailerProfileAdjuster(31803.0),
            FlaggedBatchedTrailerProfileAdjuster(flags, "batched-trailer-profile-tiered", 3.8),
            MinimumBatchedTrailerProfileAdjuster(38.0),
            PercentBatchedTrailerProfileAdjuster(-18.3),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 90875.5,
    )
}
