package com.acme.transport.terminal.traileryard.review

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the trailer yard review calculation. */
interface TrailerYardReviewAdjuster {
    fun describe(): String
    fun adjust(item: TrailerYardReview, value: Double): Double
}

data class RoundingTrailerYardReviewAdjuster(var step: Double) : TrailerYardReviewAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TrailerYardReview, value: Double): Double = roundTo(value, step)
}

data class FlaggedTrailerYardReviewAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TrailerYardReviewAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TrailerYardReview, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentTrailerYardReviewAdjuster(var percent: Double) : TrailerYardReviewAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TrailerYardReview, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TrailerYardReviewFuelLitersOffsetAdjuster(var weight: Double) : TrailerYardReviewAdjuster {
    override fun describe(): String = "plus fuel liters times $weight"

    override fun adjust(item: TrailerYardReview, value: Double): Double = value + item.fuelLiters * weight
}

data class TieredTrailerYardReviewAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TrailerYardReviewAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TrailerYardReview, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the trailer yard review steps in order and keeps a trace of each. */
data class TrailerYardReviewEngine(
    var steps: MutableList<TrailerYardReviewAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TrailerYardReview): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TrailerYardReviewAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<TrailerYardReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTrailerYardReviewEngine(flags: Registry): TrailerYardReviewEngine {
    return TrailerYardReviewEngine(
        steps = mutableListOf<TrailerYardReviewAdjuster>(
            RoundingTrailerYardReviewAdjuster(0.1),
            FlaggedTrailerYardReviewAdjuster(flags, "trailer-yard-review-rolling", -4.5),
            PercentTrailerYardReviewAdjuster(10.8),
            TrailerYardReviewFuelLitersOffsetAdjuster(0.42),
            TieredTrailerYardReviewAdjuster(
                mutableListOf<Double>(2705.0, 3243.0, 6337.0),
                mutableListOf<Double>(1.16, 0.84, 1.02),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 33126.2,
    )
}
