package com.acme.transport.linehaul.laneplan.item

import com.acme.transport.kit.numbers.roundTo

/** One step of the fallback lane plan review calculation. */
interface FallbackLanePlanReviewAdjuster {
    fun describe(): String
    fun adjust(item: FallbackLanePlanReview, value: Double): Double
}

data class LabelFallbackLanePlanReviewAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackLanePlanReviewAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: FallbackLanePlanReview, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentFallbackLanePlanReviewAdjuster(var percent: Double) : FallbackLanePlanReviewAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackLanePlanReview, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingFallbackLanePlanReviewAdjuster(var step: Double) : FallbackLanePlanReviewAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackLanePlanReview, value: Double): Double = roundTo(value, step)
}

data class TieredFallbackLanePlanReviewAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackLanePlanReviewAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackLanePlanReview, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the fallback lane plan review steps in order and keeps a trace of each. */
data class FallbackLanePlanReviewEngine(
    var steps: MutableList<FallbackLanePlanReviewAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: FallbackLanePlanReview): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackLanePlanReviewAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackLanePlanReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultFallbackLanePlanReviewEngine(): FallbackLanePlanReviewEngine {
    return FallbackLanePlanReviewEngine(
        steps = mutableListOf<FallbackLanePlanReviewAdjuster>(
            LabelFallbackLanePlanReviewAdjuster(mutableMapOf<String, Double>("retail" to 1.35, "east" to 1.09)),
            PercentFallbackLanePlanReviewAdjuster(22.1),
            RoundingFallbackLanePlanReviewAdjuster(0.25),
            TieredFallbackLanePlanReviewAdjuster(
                mutableListOf<Double>(506.0, 4437.0, 6686.0, 9348.0),
                mutableListOf<Double>(1.0, 1.15, 1.16, 0.96),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 8352.0,
    )
}
