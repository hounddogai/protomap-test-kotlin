package com.acme.transport.lastmile.attempt.template

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the attempt review calculation. */
interface AttemptReviewStep {
    fun describe(): String
    fun adjust(item: AttemptReview, value: Double): Double
}

data class MinimumAttemptReviewAdjuster(var minimum: Double) : AttemptReviewStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AttemptReview, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class AttemptReviewPriorityOffsetAdjuster(var weight: Double) : AttemptReviewStep {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: AttemptReview, value: Double): Double = value + item.priority.toDouble() * weight
}

data class NameAttemptReviewAdjuster(var factors: MutableMap<String, Double>) : AttemptReviewStep {
    override fun describe(): String = "by name"

    override fun adjust(item: AttemptReview, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredAttemptReviewAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AttemptReviewStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AttemptReview, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedAttemptReviewAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AttemptReviewStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AttemptReview, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentAttemptReviewAdjuster(var percent: Double) : AttemptReviewStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AttemptReview, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the attempt review steps in order and keeps a trace of each. */
data class AttemptReviewEngine(
    var steps: MutableList<AttemptReviewStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AttemptReview): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AttemptReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAttemptReviewEngine(flags: Registry): AttemptReviewEngine {
    return AttemptReviewEngine(
        steps = mutableListOf<AttemptReviewStep>(
            MinimumAttemptReviewAdjuster(5.4),
            AttemptReviewPriorityOffsetAdjuster(0.94),
            NameAttemptReviewAdjuster(
                mutableMapOf<String, Double>("north" to 0.82, "east" to 0.95, "central" to 0.88),
            ),
            TieredAttemptReviewAdjuster(
                mutableListOf<Double>(1370.0, 6084.0, 6123.0, 9836.0),
                mutableListOf<Double>(1.16, 1.12, 1.05, 1.15),
            ),
            FlaggedAttemptReviewAdjuster(flags, "attempt-review-tiered", -3.9),
            PercentAttemptReviewAdjuster(21.2),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 33837.9,
    )
}
