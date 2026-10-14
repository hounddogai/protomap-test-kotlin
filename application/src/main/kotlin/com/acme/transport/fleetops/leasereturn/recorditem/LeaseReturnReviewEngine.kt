package com.acme.transport.fleetops.leasereturn.recorditem

/** One step of the lease return review calculation. */
interface LeaseReturnReviewFactor {
    fun describe(): String
    fun adjust(item: LeaseReturnReview, value: Double): Double
}

data class PercentLeaseReturnReviewAdjuster(var percent: Double) : LeaseReturnReviewFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LeaseReturnReview, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class SegmentLeaseReturnReviewAdjuster(var factors: MutableMap<String, Double>) : LeaseReturnReviewFactor {
    override fun describe(): String = "by segment"

    override fun adjust(item: LeaseReturnReview, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class LeaseReturnReviewThresholdOffsetAdjuster(var weight: Double) : LeaseReturnReviewFactor {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: LeaseReturnReview, value: Double): Double = value + item.threshold * weight
}

data class TieredLeaseReturnReviewAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LeaseReturnReviewFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LeaseReturnReview, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the lease return review steps in order and keeps a trace of each. */
data class LeaseReturnReviewEngine(
    var steps: MutableList<LeaseReturnReviewFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LeaseReturnReview): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LeaseReturnReviewFactor = steps[index]
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

    fun evaluateAll(items: MutableList<LeaseReturnReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLeaseReturnReviewEngine(): LeaseReturnReviewEngine {
    return LeaseReturnReviewEngine(
        steps = mutableListOf<LeaseReturnReviewFactor>(
            PercentLeaseReturnReviewAdjuster(22.8),
            SegmentLeaseReturnReviewAdjuster(mutableMapOf<String, Double>("central" to 0.99)),
            LeaseReturnReviewThresholdOffsetAdjuster(1.75),
            TieredLeaseReturnReviewAdjuster(
                mutableListOf<Double>(5753.0, 8704.0, 9576.0),
                mutableListOf<Double>(1.02, 1.04, 0.92),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 54425.0,
    )
}
