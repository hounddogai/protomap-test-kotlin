package com.acme.transport.emissions.idling.review

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the tiered idling review calculation. */
interface TieredIdlingReviewAdjuster {
    fun describe(): String
    fun adjust(item: TieredIdlingReview, value: Double): Double
}

data class FlaggedTieredIdlingReviewAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredIdlingReviewAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredIdlingReview, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapTieredIdlingReviewAdjuster(var cap: Double) : TieredIdlingReviewAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredIdlingReview, value: Double): Double = minOf(value, cap)
}

data class MinimumTieredIdlingReviewAdjuster(var minimum: Double) : TieredIdlingReviewAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredIdlingReview, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredIdlingReviewMinimumOffsetAdjuster(var weight: Double) : TieredIdlingReviewAdjuster {
    override fun describe(): String = "plus minimum times $weight"

    override fun adjust(item: TieredIdlingReview, value: Double): Double = value + item.minimum * weight
}

data class NameTieredIdlingReviewAdjuster(
    var factors: MutableMap<String, Double>,
) : TieredIdlingReviewAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: TieredIdlingReview, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingTieredIdlingReviewAdjuster(var step: Double) : TieredIdlingReviewAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredIdlingReview, value: Double): Double = roundTo(value, step)
}

/** Runs the tiered idling review steps in order and keeps a trace of each. */
data class TieredIdlingReviewEngine(
    var steps: MutableList<TieredIdlingReviewAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TieredIdlingReview): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TieredIdlingReviewAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<TieredIdlingReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTieredIdlingReviewEngine(flags: Registry): TieredIdlingReviewEngine {
    return TieredIdlingReviewEngine(
        steps = mutableListOf<TieredIdlingReviewAdjuster>(
            FlaggedTieredIdlingReviewAdjuster(flags, "tiered-idling-review-global", -8.2),
            CapTieredIdlingReviewAdjuster(11562.0),
            MinimumTieredIdlingReviewAdjuster(37.8),
            TieredIdlingReviewMinimumOffsetAdjuster(1.24),
            NameTieredIdlingReviewAdjuster(mutableMapOf<String, Double>("east" to 1.14, "central" to 1.11)),
            RoundingTieredIdlingReviewAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 79996.1,
    )
}
