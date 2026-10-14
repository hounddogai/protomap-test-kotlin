package com.acme.transport.terminal.traileryard.yardquota

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the batched trailer yard quota calculation. */
interface BatchedTrailerYardQuotaAdjuster {
    fun describe(): String
    fun adjust(item: BatchedTrailerYardQuota, value: Double): Double
}

data class FlaggedBatchedTrailerYardQuotaAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedTrailerYardQuotaAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedTrailerYardQuota, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class BatchedTrailerYardQuotaEndsAtOffsetAdjuster(var weight: Double) : BatchedTrailerYardQuotaAdjuster {
    override fun describe(): String = "plus ends at times $weight"

    override fun adjust(item: BatchedTrailerYardQuota, value: Double): Double {
        return value + item.endsAt.toDouble() * weight
    }
}

data class RegionBatchedTrailerYardQuotaAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedTrailerYardQuotaAdjuster {
    override fun describe(): String = "by region"

    override fun adjust(item: BatchedTrailerYardQuota, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingBatchedTrailerYardQuotaAdjuster(var step: Double) : BatchedTrailerYardQuotaAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedTrailerYardQuota, value: Double): Double = roundTo(value, step)
}

/** Runs the batched trailer yard quota steps in order and keeps a trace of each. */
data class BatchedTrailerYardQuotaEngine(
    var steps: MutableList<BatchedTrailerYardQuotaAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: BatchedTrailerYardQuota): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedTrailerYardQuotaAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedTrailerYardQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultBatchedTrailerYardQuotaEngine(flags: Registry): BatchedTrailerYardQuotaEngine {
    return BatchedTrailerYardQuotaEngine(
        steps = mutableListOf<BatchedTrailerYardQuotaAdjuster>(
            FlaggedBatchedTrailerYardQuotaAdjuster(flags, "batched-trailer-yard-quota-scheduled", 3.7),
            BatchedTrailerYardQuotaEndsAtOffsetAdjuster(1.84),
            RegionBatchedTrailerYardQuotaAdjuster(
                mutableMapOf<String, Double>("online" to 0.73, "north" to 1.23),
            ),
            RoundingBatchedTrailerYardQuotaAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 13850.0,
    )
}
