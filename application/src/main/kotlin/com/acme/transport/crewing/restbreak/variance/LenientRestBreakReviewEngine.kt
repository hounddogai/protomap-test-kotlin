package com.acme.transport.crewing.restbreak.variance

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the lenient rest break review calculation. */
interface LenientRestBreakReviewFactor {
    fun describe(): String
    fun adjust(item: LenientRestBreakReview, value: Double): Double
}

data class TieredLenientRestBreakReviewAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientRestBreakReviewFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientRestBreakReview, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LenientRestBreakReviewRatioOffsetAdjuster(var weight: Double) : LenientRestBreakReviewFactor {
    override fun describe(): String = "plus ratio times $weight"

    override fun adjust(item: LenientRestBreakReview, value: Double): Double = value + item.ratio * weight
}

data class MinimumLenientRestBreakReviewAdjuster(var minimum: Double) : LenientRestBreakReviewFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientRestBreakReview, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapLenientRestBreakReviewAdjuster(var cap: Double) : LenientRestBreakReviewFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientRestBreakReview, value: Double): Double = minOf(value, cap)
}

/** Runs the lenient rest break review steps in order and keeps a trace of each. */
data class LenientRestBreakReviewEngine(
    var steps: MutableList<LenientRestBreakReviewFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LenientRestBreakReview): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LenientRestBreakReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLenientRestBreakReviewEngine(): LenientRestBreakReviewEngine {
    return LenientRestBreakReviewEngine(
        steps = mutableListOf<LenientRestBreakReviewFactor>(
            TieredLenientRestBreakReviewAdjuster(
                mutableListOf<Double>(187.0, 4839.0, 4964.0),
                mutableListOf<Double>(0.81, 1.2, 0.9),
            ),
            LenientRestBreakReviewRatioOffsetAdjuster(1.4),
            MinimumLenientRestBreakReviewAdjuster(29.9),
            CapLenientRestBreakReviewAdjuster(18633.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 57466.2,
    )
}
