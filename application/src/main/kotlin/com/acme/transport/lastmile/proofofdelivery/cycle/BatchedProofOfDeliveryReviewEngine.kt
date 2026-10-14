package com.acme.transport.lastmile.proofofdelivery.cycle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the batched proof of delivery review calculation. */
interface BatchedProofOfDeliveryReviewAdjuster {
    fun describe(): String
    fun adjust(item: BatchedProofOfDeliveryReview, value: Double): Double
}

data class RoundingBatchedProofOfDeliveryReviewAdjuster(
    var step: Double,
) : BatchedProofOfDeliveryReviewAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedProofOfDeliveryReview, value: Double): Double = roundTo(value, step)
}

data class BatchedProofOfDeliveryReviewBudgetOffsetAdjuster(
    var weight: Double,
) : BatchedProofOfDeliveryReviewAdjuster {
    override fun describe(): String = "plus budget times $weight"

    override fun adjust(item: BatchedProofOfDeliveryReview, value: Double): Double = value + item.budget * weight
}

data class CapBatchedProofOfDeliveryReviewAdjuster(var cap: Double) : BatchedProofOfDeliveryReviewAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedProofOfDeliveryReview, value: Double): Double = minOf(value, cap)
}

data class FlaggedBatchedProofOfDeliveryReviewAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedProofOfDeliveryReviewAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedProofOfDeliveryReview, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredBatchedProofOfDeliveryReviewAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedProofOfDeliveryReviewAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedProofOfDeliveryReview, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the batched proof of delivery review steps in order and keeps a trace of each. */
data class BatchedProofOfDeliveryReviewEngine(
    var steps: MutableList<BatchedProofOfDeliveryReviewAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: BatchedProofOfDeliveryReview): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedProofOfDeliveryReviewAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedProofOfDeliveryReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultBatchedProofOfDeliveryReviewEngine(flags: Registry): BatchedProofOfDeliveryReviewEngine {
    return BatchedProofOfDeliveryReviewEngine(
        steps = mutableListOf<BatchedProofOfDeliveryReviewAdjuster>(
            RoundingBatchedProofOfDeliveryReviewAdjuster(0.5),
            BatchedProofOfDeliveryReviewBudgetOffsetAdjuster(1.9),
            CapBatchedProofOfDeliveryReviewAdjuster(10857.0),
            FlaggedBatchedProofOfDeliveryReviewAdjuster(
                flags,
                "batched-proof-of-delivery-review-scheduled",
                -6.2,
            ),
            TieredBatchedProofOfDeliveryReviewAdjuster(
                mutableListOf<Double>(2314.0, 8656.0),
                mutableListOf<Double>(0.99, 1.15),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 93899.6,
    )
}
