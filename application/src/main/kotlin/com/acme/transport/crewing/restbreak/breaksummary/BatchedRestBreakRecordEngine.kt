package com.acme.transport.crewing.restbreak.breaksummary

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the batched rest break record calculation. */
interface BatchedRestBreakRecordFactor {
    fun describe(): String
    fun adjust(item: BatchedRestBreakRecord, value: Double): Double
}

data class TitleBatchedRestBreakRecordAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedRestBreakRecordFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: BatchedRestBreakRecord, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredBatchedRestBreakRecordAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedRestBreakRecordFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedRestBreakRecord, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentBatchedRestBreakRecordAdjuster(var percent: Double) : BatchedRestBreakRecordFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedRestBreakRecord, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingBatchedRestBreakRecordAdjuster(var step: Double) : BatchedRestBreakRecordFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedRestBreakRecord, value: Double): Double = roundTo(value, step)
}

data class FlaggedBatchedRestBreakRecordAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedRestBreakRecordFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedRestBreakRecord, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumBatchedRestBreakRecordAdjuster(var minimum: Double) : BatchedRestBreakRecordFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedRestBreakRecord, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the batched rest break record steps in order and keeps a trace of each. */
data class BatchedRestBreakRecordEngine(
    var steps: MutableList<BatchedRestBreakRecordFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BatchedRestBreakRecord): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedRestBreakRecordFactor = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedRestBreakRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBatchedRestBreakRecordEngine(flags: Registry): BatchedRestBreakRecordEngine {
    return BatchedRestBreakRecordEngine(
        steps = mutableListOf<BatchedRestBreakRecordFactor>(
            TitleBatchedRestBreakRecordAdjuster(mutableMapOf<String, Double>("retail" to 1.2)),
            TieredBatchedRestBreakRecordAdjuster(
                mutableListOf<Double>(3430.0, 6361.0),
                mutableListOf<Double>(1.15, 1.16),
            ),
            PercentBatchedRestBreakRecordAdjuster(-11.6),
            RoundingBatchedRestBreakRecordAdjuster(0.1),
            FlaggedBatchedRestBreakRecordAdjuster(flags, "batched-rest-break-record-manual", -8.8),
            MinimumBatchedRestBreakRecordAdjuster(30.8),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 29700.0,
    )
}
