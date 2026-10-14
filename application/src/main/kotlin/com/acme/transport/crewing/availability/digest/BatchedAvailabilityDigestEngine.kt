package com.acme.transport.crewing.availability.digest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the batched availability digest calculation. */
interface BatchedAvailabilityDigestAdjuster {
    fun describe(): String
    fun adjust(item: BatchedAvailabilityDigest, value: Double): Double
}

data class BatchedAvailabilityDigestRetriesOffsetAdjuster(
    var weight: Double,
) : BatchedAvailabilityDigestAdjuster {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: BatchedAvailabilityDigest, value: Double): Double {
        return value + item.retries.toDouble() * weight
    }
}

data class RoundingBatchedAvailabilityDigestAdjuster(var step: Double) : BatchedAvailabilityDigestAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedAvailabilityDigest, value: Double): Double = roundTo(value, step)
}

data class TieredBatchedAvailabilityDigestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedAvailabilityDigestAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedAvailabilityDigest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedBatchedAvailabilityDigestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedAvailabilityDigestAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedAvailabilityDigest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentBatchedAvailabilityDigestAdjuster(var percent: Double) : BatchedAvailabilityDigestAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedAvailabilityDigest, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the batched availability digest steps in order and keeps a trace of each. */
data class BatchedAvailabilityDigestEngine(
    var steps: MutableList<BatchedAvailabilityDigestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BatchedAvailabilityDigest): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedAvailabilityDigestAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedAvailabilityDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBatchedAvailabilityDigestEngine(flags: Registry): BatchedAvailabilityDigestEngine {
    return BatchedAvailabilityDigestEngine(
        steps = mutableListOf<BatchedAvailabilityDigestAdjuster>(
            BatchedAvailabilityDigestRetriesOffsetAdjuster(1.72),
            RoundingBatchedAvailabilityDigestAdjuster(0.5),
            TieredBatchedAvailabilityDigestAdjuster(
                mutableListOf<Double>(5934.0, 8478.0, 8905.0, 8968.0),
                mutableListOf<Double>(1.05, 1.06, 1.17, 0.95),
            ),
            FlaggedBatchedAvailabilityDigestAdjuster(flags, "batched-availability-digest-legacy", -5.5),
            PercentBatchedAvailabilityDigestAdjuster(-3.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 54181.4,
    )
}
