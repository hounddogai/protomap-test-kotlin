package com.acme.transport.network.cutoff.estimate

import com.acme.transport.kit.numbers.roundTo

/** One step of the cutoff review calculation. */
interface CutoffReviewAdjuster {
    fun describe(): String
    fun adjust(item: CutoffReview, value: Double): Double
}

data class TitleCutoffReviewAdjuster(var factors: MutableMap<String, Double>) : CutoffReviewAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: CutoffReview, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingCutoffReviewAdjuster(var step: Double) : CutoffReviewAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CutoffReview, value: Double): Double = roundTo(value, step)
}

data class PercentCutoffReviewAdjuster(var percent: Double) : CutoffReviewAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CutoffReview, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CutoffReviewAttemptsOffsetAdjuster(var weight: Double) : CutoffReviewAdjuster {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: CutoffReview, value: Double): Double = value + item.attempts.toDouble() * weight
}

data class TieredCutoffReviewAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CutoffReviewAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CutoffReview, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapCutoffReviewAdjuster(var cap: Double) : CutoffReviewAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CutoffReview, value: Double): Double = minOf(value, cap)
}

/** Runs the cutoff review steps in order and keeps a trace of each. */
data class CutoffReviewEngine(
    var steps: MutableList<CutoffReviewAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CutoffReview): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CutoffReviewAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<CutoffReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCutoffReviewEngine(): CutoffReviewEngine {
    return CutoffReviewEngine(
        steps = mutableListOf<CutoffReviewAdjuster>(
            TitleCutoffReviewAdjuster(mutableMapOf<String, Double>("east" to 0.73)),
            RoundingCutoffReviewAdjuster(0.25),
            PercentCutoffReviewAdjuster(-23.3),
            CutoffReviewAttemptsOffsetAdjuster(1.82),
            TieredCutoffReviewAdjuster(mutableListOf<Double>(834.0, 2419.0), mutableListOf<Double>(0.99, 0.82)),
            CapCutoffReviewAdjuster(39674.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 18925.4,
    )
}
