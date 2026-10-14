package com.acme.transport.safety.coaching.review

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the legacy coaching review calculation. */
interface LegacyCoachingReviewModifier {
    fun describe(): String
    fun adjust(item: LegacyCoachingReview, value: Double): Double
}

data class MinimumLegacyCoachingReviewAdjuster(var minimum: Double) : LegacyCoachingReviewModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyCoachingReview, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LegacyCoachingReviewCreatedAtOffsetAdjuster(var weight: Double) : LegacyCoachingReviewModifier {
    override fun describe(): String = "plus created at times $weight"

    override fun adjust(item: LegacyCoachingReview, value: Double): Double {
        return value + item.createdAt.toDouble() * weight
    }
}

data class PercentLegacyCoachingReviewAdjuster(var percent: Double) : LegacyCoachingReviewModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyCoachingReview, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedLegacyCoachingReviewAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyCoachingReviewModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyCoachingReview, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the legacy coaching review steps in order and keeps a trace of each. */
data class LegacyCoachingReviewEngine(
    var steps: MutableList<LegacyCoachingReviewModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LegacyCoachingReview): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LegacyCoachingReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLegacyCoachingReviewEngine(flags: Registry): LegacyCoachingReviewEngine {
    return LegacyCoachingReviewEngine(
        steps = mutableListOf<LegacyCoachingReviewModifier>(
            MinimumLegacyCoachingReviewAdjuster(32.7),
            LegacyCoachingReviewCreatedAtOffsetAdjuster(0.4),
            PercentLegacyCoachingReviewAdjuster(-20.8),
            FlaggedLegacyCoachingReviewAdjuster(flags, "legacy-coaching-review-manual", -4.4),
        ),
        trace = mutableListOf<String>(),
        floor = 3.1,
        ceiling = 90313.8,
    )
}
