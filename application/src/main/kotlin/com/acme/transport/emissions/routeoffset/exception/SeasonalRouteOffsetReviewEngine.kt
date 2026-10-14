package com.acme.transport.emissions.routeoffset.exception

import com.acme.transport.kit.flags.Registry

/** One step of the seasonal route offset review calculation. */
interface SeasonalRouteOffsetReviewAdjuster {
    fun describe(): String
    fun adjust(item: SeasonalRouteOffsetReview, value: Double): Double
}

data class TieredSeasonalRouteOffsetReviewAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalRouteOffsetReviewAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalRouteOffsetReview, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class SeasonalRouteOffsetReviewCreatedAtOffsetAdjuster(
    var weight: Double,
) : SeasonalRouteOffsetReviewAdjuster {
    override fun describe(): String = "plus created at times $weight"

    override fun adjust(item: SeasonalRouteOffsetReview, value: Double): Double {
        return value + item.createdAt.toDouble() * weight
    }
}

data class FlaggedSeasonalRouteOffsetReviewAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalRouteOffsetReviewAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalRouteOffsetReview, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the seasonal route offset review steps in order and keeps a trace of each. */
data class SeasonalRouteOffsetReviewEngine(
    var steps: MutableList<SeasonalRouteOffsetReviewAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: SeasonalRouteOffsetReview): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalRouteOffsetReviewAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalRouteOffsetReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultSeasonalRouteOffsetReviewEngine(flags: Registry): SeasonalRouteOffsetReviewEngine {
    return SeasonalRouteOffsetReviewEngine(
        steps = mutableListOf<SeasonalRouteOffsetReviewAdjuster>(
            TieredSeasonalRouteOffsetReviewAdjuster(
                mutableListOf<Double>(5484.0, 8776.0),
                mutableListOf<Double>(0.98, 0.92),
            ),
            SeasonalRouteOffsetReviewCreatedAtOffsetAdjuster(0.41),
            FlaggedSeasonalRouteOffsetReviewAdjuster(flags, "seasonal-route-offset-review-strict", 3.4),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 13305.0,
    )
}
