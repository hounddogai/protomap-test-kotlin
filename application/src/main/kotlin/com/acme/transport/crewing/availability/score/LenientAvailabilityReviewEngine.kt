package com.acme.transport.crewing.availability.score

/** One step of the lenient availability review calculation. */
interface LenientAvailabilityReviewAdjuster {
    fun describe(): String
    fun adjust(item: LenientAvailabilityReview, value: Double): Double
}

data class LabelLenientAvailabilityReviewAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientAvailabilityReviewAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: LenientAvailabilityReview, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class LenientAvailabilityReviewEtaMinutesOffsetAdjuster(
    var weight: Double,
) : LenientAvailabilityReviewAdjuster {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: LenientAvailabilityReview, value: Double): Double {
        return value + item.etaMinutes.toDouble() * weight
    }
}

data class PercentLenientAvailabilityReviewAdjuster(var percent: Double) : LenientAvailabilityReviewAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientAvailabilityReview, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredLenientAvailabilityReviewAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientAvailabilityReviewAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientAvailabilityReview, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the lenient availability review steps in order and keeps a trace of each. */
data class LenientAvailabilityReviewEngine(
    var steps: MutableList<LenientAvailabilityReviewAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LenientAvailabilityReview): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LenientAvailabilityReviewAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<LenientAvailabilityReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLenientAvailabilityReviewEngine(): LenientAvailabilityReviewEngine {
    return LenientAvailabilityReviewEngine(
        steps = mutableListOf<LenientAvailabilityReviewAdjuster>(
            LabelLenientAvailabilityReviewAdjuster(
                mutableMapOf<String, Double>("west" to 1.18, "north" to 1.2, "central" to 0.99),
            ),
            LenientAvailabilityReviewEtaMinutesOffsetAdjuster(0.39),
            PercentLenientAvailabilityReviewAdjuster(-22.2),
            TieredLenientAvailabilityReviewAdjuster(
                mutableListOf<Double>(99.0, 2630.0, 8491.0),
                mutableListOf<Double>(1.04, 0.97, 1.14),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 95511.8,
    )
}
