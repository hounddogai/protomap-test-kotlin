package com.acme.transport.customs.clearance.checkpoint

/** One step of the regional clearance review calculation. */
interface RegionalClearanceReviewAdjuster {
    fun describe(): String
    fun adjust(item: RegionalClearanceReview, value: Double): Double
}

data class RegionalClearanceReviewExpiresAtOffsetAdjuster(
    var weight: Double,
) : RegionalClearanceReviewAdjuster {
    override fun describe(): String = "plus expires at times $weight"

    override fun adjust(item: RegionalClearanceReview, value: Double): Double {
        return value + item.expiresAt.toDouble() * weight
    }
}

data class PercentRegionalClearanceReviewAdjuster(var percent: Double) : RegionalClearanceReviewAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalClearanceReview, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredRegionalClearanceReviewAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalClearanceReviewAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalClearanceReview, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the regional clearance review steps in order and keeps a trace of each. */
data class RegionalClearanceReviewEngine(
    var steps: MutableList<RegionalClearanceReviewAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RegionalClearanceReview): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalClearanceReviewAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalClearanceReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRegionalClearanceReviewEngine(): RegionalClearanceReviewEngine {
    return RegionalClearanceReviewEngine(
        steps = mutableListOf<RegionalClearanceReviewAdjuster>(
            RegionalClearanceReviewExpiresAtOffsetAdjuster(1.58),
            PercentRegionalClearanceReviewAdjuster(1.1),
            TieredRegionalClearanceReviewAdjuster(
                mutableListOf<Double>(5043.0, 6409.0),
                mutableListOf<Double>(1.0, 0.87),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 78183.9,
    )
}
