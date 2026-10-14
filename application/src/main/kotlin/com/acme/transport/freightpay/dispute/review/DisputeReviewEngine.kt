package com.acme.transport.freightpay.dispute.review

import com.acme.transport.kit.flags.Registry

/** One step of the dispute review calculation. */
interface DisputeReviewFactor {
    fun describe(): String
    fun adjust(item: DisputeReview, value: Double): Double
}

data class FlaggedDisputeReviewAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DisputeReviewFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DisputeReview, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class DisputeReviewCreatedAtOffsetAdjuster(var weight: Double) : DisputeReviewFactor {
    override fun describe(): String = "plus created at times $weight"

    override fun adjust(item: DisputeReview, value: Double): Double = value + item.createdAt.toDouble() * weight
}

data class PercentDisputeReviewAdjuster(var percent: Double) : DisputeReviewFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DisputeReview, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the dispute review steps in order and keeps a trace of each. */
data class DisputeReviewEngine(
    var steps: MutableList<DisputeReviewFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DisputeReview): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DisputeReviewFactor = steps[index]
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

    fun evaluateAll(items: MutableList<DisputeReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDisputeReviewEngine(flags: Registry): DisputeReviewEngine {
    return DisputeReviewEngine(
        steps = mutableListOf<DisputeReviewFactor>(
            FlaggedDisputeReviewAdjuster(flags, "dispute-review-fallback", 5.9),
            DisputeReviewCreatedAtOffsetAdjuster(0.47),
            PercentDisputeReviewAdjuster(6.9),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 56727.3,
    )
}
