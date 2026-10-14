package com.acme.transport.linehaul.capacity.summary

import com.acme.transport.kit.flags.Registry

/** One step of the capacity review calculation. */
interface CapacityReviewModifier {
    fun describe(): String
    fun adjust(item: CapacityReview, value: Double): Double
}

data class CapCapacityReviewAdjuster(var cap: Double) : CapacityReviewModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CapacityReview, value: Double): Double = minOf(value, cap)
}

data class DepotCapacityReviewAdjuster(var factors: MutableMap<String, Double>) : CapacityReviewModifier {
    override fun describe(): String = "by depot"

    override fun adjust(item: CapacityReview, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapacityReviewGraceDaysOffsetAdjuster(var weight: Double) : CapacityReviewModifier {
    override fun describe(): String = "plus grace days times $weight"

    override fun adjust(item: CapacityReview, value: Double): Double = value + item.graceDays.toDouble() * weight
}

data class MinimumCapacityReviewAdjuster(var minimum: Double) : CapacityReviewModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CapacityReview, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedCapacityReviewAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CapacityReviewModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CapacityReview, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the capacity review steps in order and keeps a trace of each. */
data class CapacityReviewEngine(
    var steps: MutableList<CapacityReviewModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CapacityReview): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CapacityReviewModifier = steps[index]
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

    fun evaluateAll(items: MutableList<CapacityReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCapacityReviewEngine(flags: Registry): CapacityReviewEngine {
    return CapacityReviewEngine(
        steps = mutableListOf<CapacityReviewModifier>(
            CapCapacityReviewAdjuster(7605.0),
            DepotCapacityReviewAdjuster(mutableMapOf<String, Double>("central" to 0.95, "east" to 0.81)),
            CapacityReviewGraceDaysOffsetAdjuster(1.89),
            MinimumCapacityReviewAdjuster(25.5),
            FlaggedCapacityReviewAdjuster(flags, "capacity-review-default", -8.6),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 79319.4,
    )
}
