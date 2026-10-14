package com.acme.transport.emissions.evcharge.review

import com.acme.transport.kit.numbers.roundTo

/** One step of the adaptive ev charge review calculation. */
interface AdaptiveEvChargeReviewStep {
    fun describe(): String
    fun adjust(item: AdaptiveEvChargeReview, value: Double): Double
}

data class SegmentAdaptiveEvChargeReviewAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveEvChargeReviewStep {
    override fun describe(): String = "by segment"

    override fun adjust(item: AdaptiveEvChargeReview, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentAdaptiveEvChargeReviewAdjuster(var percent: Double) : AdaptiveEvChargeReviewStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveEvChargeReview, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumAdaptiveEvChargeReviewAdjuster(var minimum: Double) : AdaptiveEvChargeReviewStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveEvChargeReview, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapAdaptiveEvChargeReviewAdjuster(var cap: Double) : AdaptiveEvChargeReviewStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveEvChargeReview, value: Double): Double = minOf(value, cap)
}

data class RoundingAdaptiveEvChargeReviewAdjuster(var step: Double) : AdaptiveEvChargeReviewStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveEvChargeReview, value: Double): Double = roundTo(value, step)
}

data class AdaptiveEvChargeReviewRetriesOffsetAdjuster(var weight: Double) : AdaptiveEvChargeReviewStep {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: AdaptiveEvChargeReview, value: Double): Double {
        return value + item.retries.toDouble() * weight
    }
}

/** Runs the adaptive ev charge review steps in order and keeps a trace of each. */
data class AdaptiveEvChargeReviewEngine(
    var steps: MutableList<AdaptiveEvChargeReviewStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AdaptiveEvChargeReview): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveEvChargeReviewStep = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveEvChargeReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAdaptiveEvChargeReviewEngine(): AdaptiveEvChargeReviewEngine {
    return AdaptiveEvChargeReviewEngine(
        steps = mutableListOf<AdaptiveEvChargeReviewStep>(
            SegmentAdaptiveEvChargeReviewAdjuster(mutableMapOf<String, Double>("north" to 1.05)),
            PercentAdaptiveEvChargeReviewAdjuster(12.9),
            MinimumAdaptiveEvChargeReviewAdjuster(33.7),
            CapAdaptiveEvChargeReviewAdjuster(21886.0),
            RoundingAdaptiveEvChargeReviewAdjuster(0.25),
            AdaptiveEvChargeReviewRetriesOffsetAdjuster(1.65),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 49946.0,
    )
}
