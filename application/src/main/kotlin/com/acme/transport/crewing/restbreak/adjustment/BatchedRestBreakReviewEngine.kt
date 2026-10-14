package com.acme.transport.crewing.restbreak.adjustment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the batched rest break review calculation. */
interface BatchedRestBreakReviewModifier {
    fun describe(): String
    fun adjust(item: BatchedRestBreakReview, value: Double): Double
}

data class RoundingBatchedRestBreakReviewAdjuster(var step: Double) : BatchedRestBreakReviewModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedRestBreakReview, value: Double): Double = roundTo(value, step)
}

data class PercentBatchedRestBreakReviewAdjuster(var percent: Double) : BatchedRestBreakReviewModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedRestBreakReview, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumBatchedRestBreakReviewAdjuster(var minimum: Double) : BatchedRestBreakReviewModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedRestBreakReview, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredBatchedRestBreakReviewAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedRestBreakReviewModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedRestBreakReview, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedBatchedRestBreakReviewAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedRestBreakReviewModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedRestBreakReview, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapBatchedRestBreakReviewAdjuster(var cap: Double) : BatchedRestBreakReviewModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedRestBreakReview, value: Double): Double = minOf(value, cap)
}

/** Runs the batched rest break review steps in order and keeps a trace of each. */
data class BatchedRestBreakReviewEngine(
    var steps: MutableList<BatchedRestBreakReviewModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BatchedRestBreakReview): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedRestBreakReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBatchedRestBreakReviewEngine(flags: Registry): BatchedRestBreakReviewEngine {
    return BatchedRestBreakReviewEngine(
        steps = mutableListOf<BatchedRestBreakReviewModifier>(
            RoundingBatchedRestBreakReviewAdjuster(5.0),
            PercentBatchedRestBreakReviewAdjuster(-21.2),
            MinimumBatchedRestBreakReviewAdjuster(42.0),
            TieredBatchedRestBreakReviewAdjuster(
                mutableListOf<Double>(1003.0, 6150.0),
                mutableListOf<Double>(0.83, 1.07),
            ),
            FlaggedBatchedRestBreakReviewAdjuster(flags, "batched-rest-break-review-lenient", 3.2),
            CapBatchedRestBreakReviewAdjuster(26299.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 8824.5,
    )
}
