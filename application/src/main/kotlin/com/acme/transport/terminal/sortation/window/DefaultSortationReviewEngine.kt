package com.acme.transport.terminal.sortation.window

import com.acme.transport.kit.numbers.roundTo

/** One step of the default sortation review calculation. */
interface DefaultSortationReviewStep {
    fun describe(): String
    fun adjust(item: DefaultSortationReview, value: Double): Double
}

data class PercentDefaultSortationReviewAdjuster(var percent: Double) : DefaultSortationReviewStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultSortationReview, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingDefaultSortationReviewAdjuster(var step: Double) : DefaultSortationReviewStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultSortationReview, value: Double): Double = roundTo(value, step)
}

data class MinimumDefaultSortationReviewAdjuster(var minimum: Double) : DefaultSortationReviewStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultSortationReview, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the default sortation review steps in order and keeps a trace of each. */
data class DefaultSortationReviewEngine(
    var steps: MutableList<DefaultSortationReviewStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DefaultSortationReview): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultSortationReviewStep = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultSortationReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDefaultSortationReviewEngine(): DefaultSortationReviewEngine {
    return DefaultSortationReviewEngine(
        steps = mutableListOf<DefaultSortationReviewStep>(
            PercentDefaultSortationReviewAdjuster(-20.1),
            RoundingDefaultSortationReviewAdjuster(0.5),
            MinimumDefaultSortationReviewAdjuster(31.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 51345.8,
    )
}
