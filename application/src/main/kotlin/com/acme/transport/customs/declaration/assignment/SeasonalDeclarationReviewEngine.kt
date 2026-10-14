package com.acme.transport.customs.declaration.assignment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the seasonal declaration review calculation. */
interface SeasonalDeclarationReviewFactor {
    fun describe(): String
    fun adjust(item: SeasonalDeclarationReview, value: Double): Double
}

data class SeasonalDeclarationReviewRateOffsetAdjuster(var weight: Double) : SeasonalDeclarationReviewFactor {
    override fun describe(): String = "plus rate times $weight"

    override fun adjust(item: SeasonalDeclarationReview, value: Double): Double = value + item.rate * weight
}

data class RoundingSeasonalDeclarationReviewAdjuster(var step: Double) : SeasonalDeclarationReviewFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalDeclarationReview, value: Double): Double = roundTo(value, step)
}

data class MinimumSeasonalDeclarationReviewAdjuster(var minimum: Double) : SeasonalDeclarationReviewFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalDeclarationReview, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedSeasonalDeclarationReviewAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalDeclarationReviewFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalDeclarationReview, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CategorySeasonalDeclarationReviewAdjuster(
    var factors: MutableMap<String, Double>,
) : SeasonalDeclarationReviewFactor {
    override fun describe(): String = "by category"

    override fun adjust(item: SeasonalDeclarationReview, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredSeasonalDeclarationReviewAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalDeclarationReviewFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalDeclarationReview, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the seasonal declaration review steps in order and keeps a trace of each. */
data class SeasonalDeclarationReviewEngine(
    var steps: MutableList<SeasonalDeclarationReviewFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SeasonalDeclarationReview): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SeasonalDeclarationReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSeasonalDeclarationReviewEngine(flags: Registry): SeasonalDeclarationReviewEngine {
    return SeasonalDeclarationReviewEngine(
        steps = mutableListOf<SeasonalDeclarationReviewFactor>(
            SeasonalDeclarationReviewRateOffsetAdjuster(0.54),
            RoundingSeasonalDeclarationReviewAdjuster(0.5),
            MinimumSeasonalDeclarationReviewAdjuster(43.1),
            FlaggedSeasonalDeclarationReviewAdjuster(flags, "seasonal-declaration-review-legacy", -6.1),
            CategorySeasonalDeclarationReviewAdjuster(
                mutableMapOf<String, Double>("east" to 1.15, "online" to 0.84),
            ),
            TieredSeasonalDeclarationReviewAdjuster(
                mutableListOf<Double>(257.0, 4493.0, 6610.0, 8159.0),
                mutableListOf<Double>(1.14, 0.86, 1.12, 1.1),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 75185.6,
    )
}
