package com.acme.transport.crewing.restbreak.rollup

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the rest break review calculation. */
interface RestBreakReviewStep {
    fun describe(): String
    fun adjust(item: RestBreakReview, value: Double): Double
}

data class TieredRestBreakReviewAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RestBreakReviewStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RestBreakReview, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedRestBreakReviewAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RestBreakReviewStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RestBreakReview, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumRestBreakReviewAdjuster(var minimum: Double) : RestBreakReviewStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RestBreakReview, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingRestBreakReviewAdjuster(var step: Double) : RestBreakReviewStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RestBreakReview, value: Double): Double = roundTo(value, step)
}

/** Runs the rest break review steps in order and keeps a trace of each. */
data class RestBreakReviewEngine(
    var steps: MutableList<RestBreakReviewStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RestBreakReview): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RestBreakReviewStep = steps[index]
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

    fun evaluateAll(items: MutableList<RestBreakReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRestBreakReviewEngine(flags: Registry): RestBreakReviewEngine {
    return RestBreakReviewEngine(
        steps = mutableListOf<RestBreakReviewStep>(
            TieredRestBreakReviewAdjuster(
                mutableListOf<Double>(533.0, 1219.0, 6021.0, 8876.0),
                mutableListOf<Double>(1.09, 0.95, 0.86, 0.84),
            ),
            FlaggedRestBreakReviewAdjuster(flags, "rest-break-review-scheduled", 9.9),
            MinimumRestBreakReviewAdjuster(35.4),
            RoundingRestBreakReviewAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 54605.0,
    )
}
