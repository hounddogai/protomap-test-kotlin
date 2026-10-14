package com.acme.transport.network.zone.digest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the seasonal zone review calculation. */
interface SeasonalZoneReviewModifier {
    fun describe(): String
    fun adjust(item: SeasonalZoneReview, value: Double): Double
}

data class MinimumSeasonalZoneReviewAdjuster(var minimum: Double) : SeasonalZoneReviewModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalZoneReview, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingSeasonalZoneReviewAdjuster(var step: Double) : SeasonalZoneReviewModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalZoneReview, value: Double): Double = roundTo(value, step)
}

data class TieredSeasonalZoneReviewAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalZoneReviewModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalZoneReview, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedSeasonalZoneReviewAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalZoneReviewModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalZoneReview, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the seasonal zone review steps in order and keeps a trace of each. */
data class SeasonalZoneReviewEngine(
    var steps: MutableList<SeasonalZoneReviewModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SeasonalZoneReview): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalZoneReviewModifier = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalZoneReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSeasonalZoneReviewEngine(flags: Registry): SeasonalZoneReviewEngine {
    return SeasonalZoneReviewEngine(
        steps = mutableListOf<SeasonalZoneReviewModifier>(
            MinimumSeasonalZoneReviewAdjuster(43.4),
            RoundingSeasonalZoneReviewAdjuster(0.1),
            TieredSeasonalZoneReviewAdjuster(
                mutableListOf<Double>(1878.0, 3878.0),
                mutableListOf<Double>(1.09, 0.86),
            ),
            FlaggedSeasonalZoneReviewAdjuster(flags, "seasonal-zone-review-manual", 7.8),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 57710.3,
    )
}
