package com.acme.transport.network.zone.recorditem

import com.acme.transport.kit.flags.Registry

/** One step of the batched zone record calculation. */
interface BatchedZoneRecordStep {
    fun describe(): String
    fun adjust(item: BatchedZoneRecord, value: Double): Double
}

data class CapBatchedZoneRecordAdjuster(var cap: Double) : BatchedZoneRecordStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedZoneRecord, value: Double): Double = minOf(value, cap)
}

data class PercentBatchedZoneRecordAdjuster(var percent: Double) : BatchedZoneRecordStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedZoneRecord, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedBatchedZoneRecordAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedZoneRecordStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedZoneRecord, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumBatchedZoneRecordAdjuster(var minimum: Double) : BatchedZoneRecordStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedZoneRecord, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class BatchedZoneRecordAttemptsOffsetAdjuster(var weight: Double) : BatchedZoneRecordStep {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: BatchedZoneRecord, value: Double): Double = value + item.attempts.toDouble() * weight
}

/** Runs the batched zone record steps in order and keeps a trace of each. */
data class BatchedZoneRecordEngine(
    var steps: MutableList<BatchedZoneRecordStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: BatchedZoneRecord): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedZoneRecordStep = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedZoneRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultBatchedZoneRecordEngine(flags: Registry): BatchedZoneRecordEngine {
    return BatchedZoneRecordEngine(
        steps = mutableListOf<BatchedZoneRecordStep>(
            CapBatchedZoneRecordAdjuster(19823.0),
            PercentBatchedZoneRecordAdjuster(16.5),
            FlaggedBatchedZoneRecordAdjuster(flags, "batched-zone-record-adaptive", -7.5),
            MinimumBatchedZoneRecordAdjuster(30.3),
            BatchedZoneRecordAttemptsOffsetAdjuster(0.13),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 69015.7,
    )
}
