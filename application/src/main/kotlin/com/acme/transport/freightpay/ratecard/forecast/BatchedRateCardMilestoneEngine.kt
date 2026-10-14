package com.acme.transport.freightpay.ratecard.forecast

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the batched rate card milestone calculation. */
interface BatchedRateCardMilestoneFactor {
    fun describe(): String
    fun adjust(item: BatchedRateCardMilestone, value: Double): Double
}

data class TitleBatchedRateCardMilestoneAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedRateCardMilestoneFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: BatchedRateCardMilestone, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedBatchedRateCardMilestoneAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedRateCardMilestoneFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedRateCardMilestone, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingBatchedRateCardMilestoneAdjuster(var step: Double) : BatchedRateCardMilestoneFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedRateCardMilestone, value: Double): Double = roundTo(value, step)
}

data class TieredBatchedRateCardMilestoneAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedRateCardMilestoneFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedRateCardMilestone, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapBatchedRateCardMilestoneAdjuster(var cap: Double) : BatchedRateCardMilestoneFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedRateCardMilestone, value: Double): Double = minOf(value, cap)
}

/** Runs the batched rate card milestone steps in order and keeps a trace of each. */
data class BatchedRateCardMilestoneEngine(
    var steps: MutableList<BatchedRateCardMilestoneFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BatchedRateCardMilestone): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedRateCardMilestoneFactor = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedRateCardMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBatchedRateCardMilestoneEngine(flags: Registry): BatchedRateCardMilestoneEngine {
    return BatchedRateCardMilestoneEngine(
        steps = mutableListOf<BatchedRateCardMilestoneFactor>(
            TitleBatchedRateCardMilestoneAdjuster(
                mutableMapOf<String, Double>("retail" to 0.86, "north" to 1.14, "south" to 0.8),
            ),
            FlaggedBatchedRateCardMilestoneAdjuster(flags, "batched-rate-card-milestone-seasonal", -0.8),
            RoundingBatchedRateCardMilestoneAdjuster(0.05),
            TieredBatchedRateCardMilestoneAdjuster(
                mutableListOf<Double>(4355.0, 5669.0),
                mutableListOf<Double>(1.17, 0.86),
            ),
            CapBatchedRateCardMilestoneAdjuster(4509.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 29157.1,
    )
}
