package com.acme.transport.fleetops.utilization.milestone

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the utilization review calculation. */
interface UtilizationReviewModifier {
    fun describe(): String
    fun adjust(item: UtilizationReview, value: Double): Double
}

data class TieredUtilizationReviewAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : UtilizationReviewModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: UtilizationReview, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapUtilizationReviewAdjuster(var cap: Double) : UtilizationReviewModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: UtilizationReview, value: Double): Double = minOf(value, cap)
}

data class RoundingUtilizationReviewAdjuster(var step: Double) : UtilizationReviewModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: UtilizationReview, value: Double): Double = roundTo(value, step)
}

data class UtilizationReviewBatchSizeOffsetAdjuster(var weight: Double) : UtilizationReviewModifier {
    override fun describe(): String = "plus batch size times $weight"

    override fun adjust(item: UtilizationReview, value: Double): Double = value + item.batchSize.toDouble() * weight
}

data class FlaggedUtilizationReviewAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : UtilizationReviewModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: UtilizationReview, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the utilization review steps in order and keeps a trace of each. */
data class UtilizationReviewEngine(
    var steps: MutableList<UtilizationReviewModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: UtilizationReview): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: UtilizationReviewModifier = steps[index]
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

    fun evaluateAll(items: MutableList<UtilizationReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultUtilizationReviewEngine(flags: Registry): UtilizationReviewEngine {
    return UtilizationReviewEngine(
        steps = mutableListOf<UtilizationReviewModifier>(
            TieredUtilizationReviewAdjuster(
                mutableListOf<Double>(453.0, 2533.0, 3546.0, 9712.0),
                mutableListOf<Double>(0.87, 0.86, 0.95, 1.06),
            ),
            CapUtilizationReviewAdjuster(17450.0),
            RoundingUtilizationReviewAdjuster(0.1),
            UtilizationReviewBatchSizeOffsetAdjuster(1.35),
            FlaggedUtilizationReviewAdjuster(flags, "utilization-review-primary", -0.3),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 73956.3,
    )
}
