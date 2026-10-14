package com.acme.transport.emissions.fuelburn.review

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the regional fuel burn review calculation. */
interface RegionalFuelBurnReviewModifier {
    fun describe(): String
    fun adjust(item: RegionalFuelBurnReview, value: Double): Double
}

data class CapRegionalFuelBurnReviewAdjuster(var cap: Double) : RegionalFuelBurnReviewModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalFuelBurnReview, value: Double): Double = minOf(value, cap)
}

data class TieredRegionalFuelBurnReviewAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalFuelBurnReviewModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalFuelBurnReview, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumRegionalFuelBurnReviewAdjuster(var minimum: Double) : RegionalFuelBurnReviewModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalFuelBurnReview, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TitleRegionalFuelBurnReviewAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalFuelBurnReviewModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: RegionalFuelBurnReview, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the regional fuel burn review steps in order and keeps a trace of each. */
data class RegionalFuelBurnReviewEngine(
    var steps: MutableList<RegionalFuelBurnReviewModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RegionalFuelBurnReview): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RegionalFuelBurnReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRegionalFuelBurnReviewEngine(): RegionalFuelBurnReviewEngine {
    return RegionalFuelBurnReviewEngine(
        steps = mutableListOf<RegionalFuelBurnReviewModifier>(
            CapRegionalFuelBurnReviewAdjuster(7092.0),
            TieredRegionalFuelBurnReviewAdjuster(
                mutableListOf<Double>(7645.0, 8745.0),
                mutableListOf<Double>(1.13, 0.97),
            ),
            MinimumRegionalFuelBurnReviewAdjuster(13.4),
            TitleRegionalFuelBurnReviewAdjuster(mutableMapOf<String, Double>("north" to 1.27)),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 66187.9,
    )
}
