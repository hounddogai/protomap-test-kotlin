package com.acme.transport.crewing.qualification.tieredqualificationdigest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the batched qualification checkpoint calculation. */
interface BatchedQualificationCheckpointFactor {
    fun describe(): String
    fun adjust(item: BatchedQualificationCheckpoint, value: Double): Double
}

data class UnitBatchedQualificationCheckpointAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedQualificationCheckpointFactor {
    override fun describe(): String = "by unit"

    override fun adjust(item: BatchedQualificationCheckpoint, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredBatchedQualificationCheckpointAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedQualificationCheckpointFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedQualificationCheckpoint, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingBatchedQualificationCheckpointAdjuster(
    var step: Double,
) : BatchedQualificationCheckpointFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedQualificationCheckpoint, value: Double): Double = roundTo(value, step)
}

data class PercentBatchedQualificationCheckpointAdjuster(
    var percent: Double,
) : BatchedQualificationCheckpointFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedQualificationCheckpoint, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class BatchedQualificationCheckpointCapacityOffsetAdjuster(
    var weight: Double,
) : BatchedQualificationCheckpointFactor {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: BatchedQualificationCheckpoint, value: Double): Double {
        return value + item.capacity.toDouble() * weight
    }
}

data class FlaggedBatchedQualificationCheckpointAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedQualificationCheckpointFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedQualificationCheckpoint, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the batched qualification checkpoint steps in order and keeps a trace of each. */
data class BatchedQualificationCheckpointEngine(
    var steps: MutableList<BatchedQualificationCheckpointFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: BatchedQualificationCheckpoint): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedQualificationCheckpointFactor = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedQualificationCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultBatchedQualificationCheckpointEngine(flags: Registry): BatchedQualificationCheckpointEngine {
    return BatchedQualificationCheckpointEngine(
        steps = mutableListOf<BatchedQualificationCheckpointFactor>(
            UnitBatchedQualificationCheckpointAdjuster(
                mutableMapOf<String, Double>("south" to 1.29, "west" to 0.93, "retail" to 0.97),
            ),
            TieredBatchedQualificationCheckpointAdjuster(
                mutableListOf<Double>(6467.0, 8430.0),
                mutableListOf<Double>(1.12, 1.04),
            ),
            RoundingBatchedQualificationCheckpointAdjuster(1.0),
            PercentBatchedQualificationCheckpointAdjuster(-3.3),
            BatchedQualificationCheckpointCapacityOffsetAdjuster(0.41),
            FlaggedBatchedQualificationCheckpointAdjuster(flags, "batched-qualification-checkpoint-capped", 6.7),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 95233.7,
    )
}
