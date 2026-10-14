package com.acme.transport.emissions.fuelburn.burnbudget

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the weighted fuel burn review calculation. */
interface WeightedFuelBurnReviewStep {
    fun describe(): String
    fun adjust(item: WeightedFuelBurnReview, value: Double): Double
}

data class RoundingWeightedFuelBurnReviewAdjuster(var step: Double) : WeightedFuelBurnReviewStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedFuelBurnReview, value: Double): Double = roundTo(value, step)
}

data class FlaggedWeightedFuelBurnReviewAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedFuelBurnReviewStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedFuelBurnReview, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class NameWeightedFuelBurnReviewAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedFuelBurnReviewStep {
    override fun describe(): String = "by name"

    override fun adjust(item: WeightedFuelBurnReview, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentWeightedFuelBurnReviewAdjuster(var percent: Double) : WeightedFuelBurnReviewStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedFuelBurnReview, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumWeightedFuelBurnReviewAdjuster(var minimum: Double) : WeightedFuelBurnReviewStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedFuelBurnReview, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredWeightedFuelBurnReviewAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedFuelBurnReviewStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedFuelBurnReview, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the weighted fuel burn review steps in order and keeps a trace of each. */
data class WeightedFuelBurnReviewEngine(
    var steps: MutableList<WeightedFuelBurnReviewStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: WeightedFuelBurnReview): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedFuelBurnReviewStep = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedFuelBurnReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultWeightedFuelBurnReviewEngine(flags: Registry): WeightedFuelBurnReviewEngine {
    return WeightedFuelBurnReviewEngine(
        steps = mutableListOf<WeightedFuelBurnReviewStep>(
            RoundingWeightedFuelBurnReviewAdjuster(0.5),
            FlaggedWeightedFuelBurnReviewAdjuster(flags, "weighted-fuel-burn-review-pending", -1.9),
            NameWeightedFuelBurnReviewAdjuster(
                mutableMapOf<String, Double>("central" to 0.82, "west" to 1.14, "retail" to 1.35),
            ),
            PercentWeightedFuelBurnReviewAdjuster(1.7),
            MinimumWeightedFuelBurnReviewAdjuster(5.6),
            TieredWeightedFuelBurnReviewAdjuster(
                mutableListOf<Double>(4969.0, 5988.0, 8435.0),
                mutableListOf<Double>(0.89, 0.85, 1.03),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 51862.4,
    )
}
