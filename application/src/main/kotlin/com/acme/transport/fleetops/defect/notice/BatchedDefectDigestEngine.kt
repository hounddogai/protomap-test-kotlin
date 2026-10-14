package com.acme.transport.fleetops.defect.notice

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the batched defect digest calculation. */
interface BatchedDefectDigestStep {
    fun describe(): String
    fun adjust(item: BatchedDefectDigest, value: Double): Double
}

data class CapBatchedDefectDigestAdjuster(var cap: Double) : BatchedDefectDigestStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedDefectDigest, value: Double): Double = minOf(value, cap)
}

data class FlaggedBatchedDefectDigestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedDefectDigestStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedDefectDigest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingBatchedDefectDigestAdjuster(var step: Double) : BatchedDefectDigestStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedDefectDigest, value: Double): Double = roundTo(value, step)
}

data class PercentBatchedDefectDigestAdjuster(var percent: Double) : BatchedDefectDigestStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedDefectDigest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class BatchedDefectDigestQuantityOffsetAdjuster(var weight: Double) : BatchedDefectDigestStep {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: BatchedDefectDigest, value: Double): Double = value + item.quantity.toDouble() * weight
}

data class SourceBatchedDefectDigestAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedDefectDigestStep {
    override fun describe(): String = "by source"

    override fun adjust(item: BatchedDefectDigest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the batched defect digest steps in order and keeps a trace of each. */
data class BatchedDefectDigestEngine(
    var steps: MutableList<BatchedDefectDigestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: BatchedDefectDigest): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedDefectDigestStep = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedDefectDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultBatchedDefectDigestEngine(flags: Registry): BatchedDefectDigestEngine {
    return BatchedDefectDigestEngine(
        steps = mutableListOf<BatchedDefectDigestStep>(
            CapBatchedDefectDigestAdjuster(31696.0),
            FlaggedBatchedDefectDigestAdjuster(flags, "batched-defect-digest-tiered", 4.1),
            RoundingBatchedDefectDigestAdjuster(5.0),
            PercentBatchedDefectDigestAdjuster(16.5),
            BatchedDefectDigestQuantityOffsetAdjuster(1.23),
            SourceBatchedDefectDigestAdjuster(mutableMapOf<String, Double>("south" to 1.02)),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 21170.6,
    )
}
