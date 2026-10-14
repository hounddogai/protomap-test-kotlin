package com.acme.transport.lastmile.proofofdelivery.audit

import com.acme.transport.kit.flags.Registry

/** One step of the weighted proof of delivery review calculation. */
interface WeightedProofOfDeliveryReviewStep {
    fun describe(): String
    fun adjust(item: WeightedProofOfDeliveryReview, value: Double): Double
}

data class CapWeightedProofOfDeliveryReviewAdjuster(var cap: Double) : WeightedProofOfDeliveryReviewStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedProofOfDeliveryReview, value: Double): Double = minOf(value, cap)
}

data class FlaggedWeightedProofOfDeliveryReviewAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedProofOfDeliveryReviewStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedProofOfDeliveryReview, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredWeightedProofOfDeliveryReviewAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedProofOfDeliveryReviewStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedProofOfDeliveryReview, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class UnitWeightedProofOfDeliveryReviewAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedProofOfDeliveryReviewStep {
    override fun describe(): String = "by unit"

    override fun adjust(item: WeightedProofOfDeliveryReview, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentWeightedProofOfDeliveryReviewAdjuster(
    var percent: Double,
) : WeightedProofOfDeliveryReviewStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedProofOfDeliveryReview, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the weighted proof of delivery review steps in order and keeps a trace of each. */
data class WeightedProofOfDeliveryReviewEngine(
    var steps: MutableList<WeightedProofOfDeliveryReviewStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: WeightedProofOfDeliveryReview): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedProofOfDeliveryReviewStep = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedProofOfDeliveryReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultWeightedProofOfDeliveryReviewEngine(flags: Registry): WeightedProofOfDeliveryReviewEngine {
    return WeightedProofOfDeliveryReviewEngine(
        steps = mutableListOf<WeightedProofOfDeliveryReviewStep>(
            CapWeightedProofOfDeliveryReviewAdjuster(27143.0),
            FlaggedWeightedProofOfDeliveryReviewAdjuster(
                flags,
                "weighted-proof-of-delivery-review-seasonal",
                -5.0,
            ),
            TieredWeightedProofOfDeliveryReviewAdjuster(
                mutableListOf<Double>(302.0, 2332.0, 4136.0),
                mutableListOf<Double>(0.96, 1.08, 0.99),
            ),
            UnitWeightedProofOfDeliveryReviewAdjuster(
                mutableMapOf<String, Double>("south" to 1.01, "east" to 1.36),
            ),
            PercentWeightedProofOfDeliveryReviewAdjuster(-10.8),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 31052.4,
    )
}
