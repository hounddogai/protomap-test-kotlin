package com.acme.transport.customs.clearance.clearancecheckpoint

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the batched clearance checkpoint calculation. */
interface BatchedClearanceCheckpointFactor {
    fun describe(): String
    fun adjust(item: BatchedClearanceCheckpoint, value: Double): Double
}

data class TieredBatchedClearanceCheckpointAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedClearanceCheckpointFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedClearanceCheckpoint, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedBatchedClearanceCheckpointAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedClearanceCheckpointFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedClearanceCheckpoint, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class BatchedClearanceCheckpointRateOffsetAdjuster(var weight: Double) : BatchedClearanceCheckpointFactor {
    override fun describe(): String = "plus rate times $weight"

    override fun adjust(item: BatchedClearanceCheckpoint, value: Double): Double = value + item.rate * weight
}

data class RoundingBatchedClearanceCheckpointAdjuster(var step: Double) : BatchedClearanceCheckpointFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedClearanceCheckpoint, value: Double): Double = roundTo(value, step)
}

data class PercentBatchedClearanceCheckpointAdjuster(var percent: Double) : BatchedClearanceCheckpointFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedClearanceCheckpoint, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the batched clearance checkpoint steps in order and keeps a trace of each. */
data class BatchedClearanceCheckpointEngine(
    var steps: MutableList<BatchedClearanceCheckpointFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BatchedClearanceCheckpoint): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedClearanceCheckpointFactor = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedClearanceCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBatchedClearanceCheckpointEngine(flags: Registry): BatchedClearanceCheckpointEngine {
    return BatchedClearanceCheckpointEngine(
        steps = mutableListOf<BatchedClearanceCheckpointFactor>(
            TieredBatchedClearanceCheckpointAdjuster(
                mutableListOf<Double>(4320.0, 4424.0),
                mutableListOf<Double>(1.01, 0.97),
            ),
            FlaggedBatchedClearanceCheckpointAdjuster(flags, "batched-clearance-checkpoint-capped", 6.4),
            BatchedClearanceCheckpointRateOffsetAdjuster(0.2),
            RoundingBatchedClearanceCheckpointAdjuster(0.5),
            PercentBatchedClearanceCheckpointAdjuster(10.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 16273.6,
    )
}
