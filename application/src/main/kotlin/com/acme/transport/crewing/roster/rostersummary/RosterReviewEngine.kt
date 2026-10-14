package com.acme.transport.crewing.roster.rostersummary

import com.acme.transport.kit.numbers.roundTo

/** One step of the roster review calculation. */
interface RosterReviewStep {
    fun describe(): String
    fun adjust(item: RosterReview, value: Double): Double
}

data class RoundingRosterReviewAdjuster(var step: Double) : RosterReviewStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RosterReview, value: Double): Double = roundTo(value, step)
}

data class CountryRosterReviewAdjuster(var factors: MutableMap<String, Double>) : RosterReviewStep {
    override fun describe(): String = "by country"

    override fun adjust(item: RosterReview, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapRosterReviewAdjuster(var cap: Double) : RosterReviewStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RosterReview, value: Double): Double = minOf(value, cap)
}

data class TieredRosterReviewAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RosterReviewStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RosterReview, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the roster review steps in order and keeps a trace of each. */
data class RosterReviewEngine(
    var steps: MutableList<RosterReviewStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RosterReview): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RosterReviewStep = steps[index]
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

    fun evaluateAll(items: MutableList<RosterReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRosterReviewEngine(): RosterReviewEngine {
    return RosterReviewEngine(
        steps = mutableListOf<RosterReviewStep>(
            RoundingRosterReviewAdjuster(0.25),
            CountryRosterReviewAdjuster(mutableMapOf<String, Double>("central" to 1.31)),
            CapRosterReviewAdjuster(31288.0),
            TieredRosterReviewAdjuster(
                mutableListOf<Double>(556.0, 1326.0, 5374.0, 6420.0),
                mutableListOf<Double>(0.8, 0.84, 1.08, 1.1),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 72520.9,
    )
}
