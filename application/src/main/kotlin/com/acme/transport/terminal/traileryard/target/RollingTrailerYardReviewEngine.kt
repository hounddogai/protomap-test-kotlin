package com.acme.transport.terminal.traileryard.target

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the rolling trailer yard review calculation. */
interface RollingTrailerYardReviewFactor {
    fun describe(): String
    fun adjust(item: RollingTrailerYardReview, value: Double): Double
}

data class UnitRollingTrailerYardReviewAdjuster(
    var factors: MutableMap<String, Double>,
) : RollingTrailerYardReviewFactor {
    override fun describe(): String = "by unit"

    override fun adjust(item: RollingTrailerYardReview, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumRollingTrailerYardReviewAdjuster(var minimum: Double) : RollingTrailerYardReviewFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingTrailerYardReview, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingRollingTrailerYardReviewAdjuster(var step: Double) : RollingTrailerYardReviewFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingTrailerYardReview, value: Double): Double = roundTo(value, step)
}

data class PercentRollingTrailerYardReviewAdjuster(var percent: Double) : RollingTrailerYardReviewFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingTrailerYardReview, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedRollingTrailerYardReviewAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingTrailerYardReviewFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingTrailerYardReview, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the rolling trailer yard review steps in order and keeps a trace of each. */
data class RollingTrailerYardReviewEngine(
    var steps: MutableList<RollingTrailerYardReviewFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RollingTrailerYardReview): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingTrailerYardReviewFactor = steps[index]
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

    fun evaluateAll(items: MutableList<RollingTrailerYardReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRollingTrailerYardReviewEngine(flags: Registry): RollingTrailerYardReviewEngine {
    return RollingTrailerYardReviewEngine(
        steps = mutableListOf<RollingTrailerYardReviewFactor>(
            UnitRollingTrailerYardReviewAdjuster(mutableMapOf<String, Double>("central" to 1.15)),
            MinimumRollingTrailerYardReviewAdjuster(7.3),
            RoundingRollingTrailerYardReviewAdjuster(5.0),
            PercentRollingTrailerYardReviewAdjuster(-22.6),
            FlaggedRollingTrailerYardReviewAdjuster(flags, "rolling-trailer-yard-review-adaptive", 4.7),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 42804.8,
    )
}
