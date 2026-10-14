package com.acme.transport.safety.scorecard.scorecardproposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the default scorecard review calculation. */
interface DefaultScorecardReviewModifier {
    fun describe(): String
    fun adjust(item: DefaultScorecardReview, value: Double): Double
}

data class FlaggedDefaultScorecardReviewAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultScorecardReviewModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultScorecardReview, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TitleDefaultScorecardReviewAdjuster(
    var factors: MutableMap<String, Double>,
) : DefaultScorecardReviewModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: DefaultScorecardReview, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class DefaultScorecardReviewDurationMinutesOffsetAdjuster(
    var weight: Double,
) : DefaultScorecardReviewModifier {
    override fun describe(): String = "plus duration minutes times $weight"

    override fun adjust(item: DefaultScorecardReview, value: Double): Double {
        return value + item.durationMinutes.toDouble() * weight
    }
}

/** Runs the default scorecard review steps in order and keeps a trace of each. */
data class DefaultScorecardReviewEngine(
    var steps: MutableList<DefaultScorecardReviewModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DefaultScorecardReview): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DefaultScorecardReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDefaultScorecardReviewEngine(flags: Registry): DefaultScorecardReviewEngine {
    return DefaultScorecardReviewEngine(
        steps = mutableListOf<DefaultScorecardReviewModifier>(
            FlaggedDefaultScorecardReviewAdjuster(flags, "default-scorecard-review-strict", 0.5),
            TitleDefaultScorecardReviewAdjuster(mutableMapOf<String, Double>("west" to 1.14)),
            DefaultScorecardReviewDurationMinutesOffsetAdjuster(1.09),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 45395.7,
    )
}
