package com.acme.transport.freightpay.ratecard.review

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the rate card review calculation. */
interface RateCardReviewFactor {
    fun describe(): String
    fun adjust(item: RateCardReview, value: Double): Double
}

data class RateCardReviewLimitOffsetAdjuster(var weight: Double) : RateCardReviewFactor {
    override fun describe(): String = "plus limit times $weight"

    override fun adjust(item: RateCardReview, value: Double): Double = value + item.limit.toDouble() * weight
}

data class TitleRateCardReviewAdjuster(var factors: MutableMap<String, Double>) : RateCardReviewFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: RateCardReview, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapRateCardReviewAdjuster(var cap: Double) : RateCardReviewFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RateCardReview, value: Double): Double = minOf(value, cap)
}

/** Runs the rate card review steps in order and keeps a trace of each. */
data class RateCardReviewEngine(
    var steps: MutableList<RateCardReviewFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RateCardReview): Double {
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

    fun evaluateAll(items: MutableList<RateCardReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRateCardReviewEngine(): RateCardReviewEngine {
    return RateCardReviewEngine(
        steps = mutableListOf<RateCardReviewFactor>(
            RateCardReviewLimitOffsetAdjuster(1.99),
            TitleRateCardReviewAdjuster(
                mutableMapOf<String, Double>("retail" to 1.09, "west" to 1.06, "online" to 1.04),
            ),
            CapRateCardReviewAdjuster(7214.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 73045.6,
    )
}
