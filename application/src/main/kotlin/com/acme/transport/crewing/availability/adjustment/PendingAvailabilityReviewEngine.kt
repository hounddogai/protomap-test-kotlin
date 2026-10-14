package com.acme.transport.crewing.availability.adjustment

import com.acme.transport.kit.numbers.roundTo

/** One step of the pending availability review calculation. */
interface PendingAvailabilityReviewFactor {
    fun describe(): String
    fun adjust(item: PendingAvailabilityReview, value: Double): Double
}

data class TieredPendingAvailabilityReviewAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingAvailabilityReviewFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingAvailabilityReview, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LabelPendingAvailabilityReviewAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingAvailabilityReviewFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: PendingAvailabilityReview, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingPendingAvailabilityReviewAdjuster(var step: Double) : PendingAvailabilityReviewFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingAvailabilityReview, value: Double): Double = roundTo(value, step)
}

data class MinimumPendingAvailabilityReviewAdjuster(var minimum: Double) : PendingAvailabilityReviewFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingAvailabilityReview, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the pending availability review steps in order and keeps a trace of each. */
data class PendingAvailabilityReviewEngine(
    var steps: MutableList<PendingAvailabilityReviewFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PendingAvailabilityReview): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingAvailabilityReviewFactor = steps[index]
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

    fun evaluateAll(items: MutableList<PendingAvailabilityReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPendingAvailabilityReviewEngine(): PendingAvailabilityReviewEngine {
    return PendingAvailabilityReviewEngine(
        steps = mutableListOf<PendingAvailabilityReviewFactor>(
            TieredPendingAvailabilityReviewAdjuster(
                mutableListOf<Double>(3683.0, 7548.0, 7892.0),
                mutableListOf<Double>(1.07, 0.94, 1.03),
            ),
            LabelPendingAvailabilityReviewAdjuster(
                mutableMapOf<String, Double>("online" to 0.99, "south" to 1.04),
            ),
            RoundingPendingAvailabilityReviewAdjuster(1.0),
            MinimumPendingAvailabilityReviewAdjuster(46.5),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 14527.5,
    )
}
