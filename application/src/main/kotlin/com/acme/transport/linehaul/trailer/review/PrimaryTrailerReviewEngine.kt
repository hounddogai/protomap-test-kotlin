package com.acme.transport.linehaul.trailer.review

import com.acme.transport.kit.numbers.roundTo

/** One step of the primary trailer review calculation. */
interface PrimaryTrailerReviewStep {
    fun describe(): String
    fun adjust(item: PrimaryTrailerReview, value: Double): Double
}

data class RoundingPrimaryTrailerReviewAdjuster(var step: Double) : PrimaryTrailerReviewStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryTrailerReview, value: Double): Double = roundTo(value, step)
}

data class PercentPrimaryTrailerReviewAdjuster(var percent: Double) : PrimaryTrailerReviewStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryTrailerReview, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class PrimaryTrailerReviewMinimumOffsetAdjuster(var weight: Double) : PrimaryTrailerReviewStep {
    override fun describe(): String = "plus minimum times $weight"

    override fun adjust(item: PrimaryTrailerReview, value: Double): Double = value + item.minimum * weight
}

/** Runs the primary trailer review steps in order and keeps a trace of each. */
data class PrimaryTrailerReviewEngine(
    var steps: MutableList<PrimaryTrailerReviewStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PrimaryTrailerReview): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryTrailerReviewStep = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryTrailerReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPrimaryTrailerReviewEngine(): PrimaryTrailerReviewEngine {
    return PrimaryTrailerReviewEngine(
        steps = mutableListOf<PrimaryTrailerReviewStep>(
            RoundingPrimaryTrailerReviewAdjuster(0.25),
            PercentPrimaryTrailerReviewAdjuster(-7.8),
            PrimaryTrailerReviewMinimumOffsetAdjuster(1.43),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 37081.9,
    )
}
