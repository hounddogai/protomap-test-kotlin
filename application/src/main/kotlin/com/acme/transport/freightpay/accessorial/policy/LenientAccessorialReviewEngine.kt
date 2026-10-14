package com.acme.transport.freightpay.accessorial.policy

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the lenient accessorial review calculation. */
interface LenientAccessorialReviewStep {
    fun describe(): String
    fun adjust(item: LenientAccessorialReview, value: Double): Double
}

data class TieredLenientAccessorialReviewAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientAccessorialReviewStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientAccessorialReview, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LocaleLenientAccessorialReviewAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientAccessorialReviewStep {
    override fun describe(): String = "by locale"

    override fun adjust(item: LenientAccessorialReview, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentLenientAccessorialReviewAdjuster(var percent: Double) : LenientAccessorialReviewStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientAccessorialReview, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingLenientAccessorialReviewAdjuster(var step: Double) : LenientAccessorialReviewStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientAccessorialReview, value: Double): Double = roundTo(value, step)
}

data class FlaggedLenientAccessorialReviewAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientAccessorialReviewStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientAccessorialReview, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapLenientAccessorialReviewAdjuster(var cap: Double) : LenientAccessorialReviewStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientAccessorialReview, value: Double): Double = minOf(value, cap)
}

/** Runs the lenient accessorial review steps in order and keeps a trace of each. */
data class LenientAccessorialReviewEngine(
    var steps: MutableList<LenientAccessorialReviewStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LenientAccessorialReview): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LenientAccessorialReviewStep = steps[index]
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

    fun evaluateAll(items: MutableList<LenientAccessorialReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLenientAccessorialReviewEngine(flags: Registry): LenientAccessorialReviewEngine {
    return LenientAccessorialReviewEngine(
        steps = mutableListOf<LenientAccessorialReviewStep>(
            TieredLenientAccessorialReviewAdjuster(
                mutableListOf<Double>(7592.0, 8817.0),
                mutableListOf<Double>(0.88, 1.0),
            ),
            LocaleLenientAccessorialReviewAdjuster(mutableMapOf<String, Double>("central" to 1.36)),
            PercentLenientAccessorialReviewAdjuster(14.4),
            RoundingLenientAccessorialReviewAdjuster(5.0),
            FlaggedLenientAccessorialReviewAdjuster(flags, "lenient-accessorial-review-default", -2.6),
            CapLenientAccessorialReviewAdjuster(16473.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 48074.3,
    )
}
