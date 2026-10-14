package com.acme.transport.lastmile.proofofdelivery.deliveryreview

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the regional proof of delivery review calculation. */
interface RegionalProofOfDeliveryReviewAdjuster {
    fun describe(): String
    fun adjust(item: RegionalProofOfDeliveryReview, value: Double): Double
}

data class NameRegionalProofOfDeliveryReviewAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalProofOfDeliveryReviewAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: RegionalProofOfDeliveryReview, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumRegionalProofOfDeliveryReviewAdjuster(
    var minimum: Double,
) : RegionalProofOfDeliveryReviewAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalProofOfDeliveryReview, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RegionalProofOfDeliveryReviewEndsAtOffsetAdjuster(
    var weight: Double,
) : RegionalProofOfDeliveryReviewAdjuster {
    override fun describe(): String = "plus ends at times $weight"

    override fun adjust(item: RegionalProofOfDeliveryReview, value: Double): Double {
        return value + item.endsAt.toDouble() * weight
    }
}

data class FlaggedRegionalProofOfDeliveryReviewAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalProofOfDeliveryReviewAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalProofOfDeliveryReview, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapRegionalProofOfDeliveryReviewAdjuster(var cap: Double) : RegionalProofOfDeliveryReviewAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalProofOfDeliveryReview, value: Double): Double = minOf(value, cap)
}

/** Runs the regional proof of delivery review steps in order and keeps a trace of each. */
data class RegionalProofOfDeliveryReviewEngine(
    var steps: MutableList<RegionalProofOfDeliveryReviewAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RegionalProofOfDeliveryReview): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RegionalProofOfDeliveryReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRegionalProofOfDeliveryReviewEngine(flags: Registry): RegionalProofOfDeliveryReviewEngine {
    return RegionalProofOfDeliveryReviewEngine(
        steps = mutableListOf<RegionalProofOfDeliveryReviewAdjuster>(
            NameRegionalProofOfDeliveryReviewAdjuster(mutableMapOf<String, Double>("north" to 1.11)),
            MinimumRegionalProofOfDeliveryReviewAdjuster(49.5),
            RegionalProofOfDeliveryReviewEndsAtOffsetAdjuster(0.37),
            FlaggedRegionalProofOfDeliveryReviewAdjuster(
                flags,
                "regional-proof-of-delivery-review-pending",
                4.9,
            ),
            CapRegionalProofOfDeliveryReviewAdjuster(38472.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 95751.2,
    )
}
