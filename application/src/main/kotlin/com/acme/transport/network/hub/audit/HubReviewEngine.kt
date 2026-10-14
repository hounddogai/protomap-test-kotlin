package com.acme.transport.network.hub.audit

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the hub review calculation. */
interface HubReviewFactor {
    fun describe(): String
    fun adjust(item: HubReview, value: Double): Double
}

data class HubReviewCreatedAtOffsetAdjuster(var weight: Double) : HubReviewFactor {
    override fun describe(): String = "plus created at times $weight"

    override fun adjust(item: HubReview, value: Double): Double = value + item.createdAt.toDouble() * weight
}

data class RoundingHubReviewAdjuster(var step: Double) : HubReviewFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: HubReview, value: Double): Double = roundTo(value, step)
}

data class CapHubReviewAdjuster(var cap: Double) : HubReviewFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: HubReview, value: Double): Double = minOf(value, cap)
}

data class TitleHubReviewAdjuster(var factors: MutableMap<String, Double>) : HubReviewFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: HubReview, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedHubReviewAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : HubReviewFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: HubReview, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredHubReviewAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : HubReviewFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: HubReview, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the hub review steps in order and keeps a trace of each. */
data class HubReviewEngine(
    var steps: MutableList<HubReviewFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: HubReview): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: HubReviewFactor = steps[index]
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

    fun evaluateAll(items: MutableList<HubReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultHubReviewEngine(flags: Registry): HubReviewEngine {
    return HubReviewEngine(
        steps = mutableListOf<HubReviewFactor>(
            HubReviewCreatedAtOffsetAdjuster(1.6),
            RoundingHubReviewAdjuster(1.0),
            CapHubReviewAdjuster(21848.0),
            TitleHubReviewAdjuster(mutableMapOf<String, Double>("online" to 1.2)),
            FlaggedHubReviewAdjuster(flags, "hub-review-batched", 0.4),
            TieredHubReviewAdjuster(mutableListOf<Double>(236.0, 3902.0), mutableListOf<Double>(1.0, 1.02)),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 72717.3,
    )
}
