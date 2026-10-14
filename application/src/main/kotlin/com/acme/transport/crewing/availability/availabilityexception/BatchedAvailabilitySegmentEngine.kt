package com.acme.transport.crewing.availability.availabilityexception

import com.acme.transport.kit.flags.Registry

/** One step of the batched availability segment calculation. */
interface BatchedAvailabilitySegmentStep {
    fun describe(): String
    fun adjust(item: BatchedAvailabilitySegment, value: Double): Double
}

data class FlaggedBatchedAvailabilitySegmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedAvailabilitySegmentStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedAvailabilitySegment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentBatchedAvailabilitySegmentAdjuster(var percent: Double) : BatchedAvailabilitySegmentStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedAvailabilitySegment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumBatchedAvailabilitySegmentAdjuster(var minimum: Double) : BatchedAvailabilitySegmentStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedAvailabilitySegment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the batched availability segment steps in order and keeps a trace of each. */
data class BatchedAvailabilitySegmentEngine(
    var steps: MutableList<BatchedAvailabilitySegmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: BatchedAvailabilitySegment): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedAvailabilitySegmentStep = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedAvailabilitySegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultBatchedAvailabilitySegmentEngine(flags: Registry): BatchedAvailabilitySegmentEngine {
    return BatchedAvailabilitySegmentEngine(
        steps = mutableListOf<BatchedAvailabilitySegmentStep>(
            FlaggedBatchedAvailabilitySegmentAdjuster(flags, "batched-availability-segment-rolling", 2.6),
            PercentBatchedAvailabilitySegmentAdjuster(20.3),
            MinimumBatchedAvailabilitySegmentAdjuster(45.9),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 8668.4,
    )
}
