package com.acme.transport.customs.clearance.milestone

import com.acme.transport.kit.flags.Registry

/** One step of the clearance review calculation. */
interface ClearanceReviewAdjuster {
    fun describe(): String
    fun adjust(item: ClearanceReview, value: Double): Double
}

data class MinimumClearanceReviewAdjuster(var minimum: Double) : ClearanceReviewAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ClearanceReview, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedClearanceReviewAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ClearanceReviewAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ClearanceReview, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapClearanceReviewAdjuster(var cap: Double) : ClearanceReviewAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ClearanceReview, value: Double): Double = minOf(value, cap)
}

data class TieredClearanceReviewAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ClearanceReviewAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ClearanceReview, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the clearance review steps in order and keeps a trace of each. */
data class ClearanceReviewEngine(
    var steps: MutableList<ClearanceReviewAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ClearanceReview): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ClearanceReviewAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ClearanceReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultClearanceReviewEngine(flags: Registry): ClearanceReviewEngine {
    return ClearanceReviewEngine(
        steps = mutableListOf<ClearanceReviewAdjuster>(
            MinimumClearanceReviewAdjuster(35.3),
            FlaggedClearanceReviewAdjuster(flags, "clearance-review-lenient", -7.9),
            CapClearanceReviewAdjuster(10085.0),
            TieredClearanceReviewAdjuster(
                mutableListOf<Double>(5257.0, 5411.0),
                mutableListOf<Double>(0.87, 1.12),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 39380.6,
    )
}
