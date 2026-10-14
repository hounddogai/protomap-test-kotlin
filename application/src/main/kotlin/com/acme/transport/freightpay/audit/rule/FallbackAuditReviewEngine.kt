package com.acme.transport.freightpay.audit.rule

import com.acme.transport.kit.numbers.roundTo

/** One step of the fallback audit review calculation. */
interface FallbackAuditReviewAdjuster {
    fun describe(): String
    fun adjust(item: FallbackAuditReview, value: Double): Double
}

data class TitleFallbackAuditReviewAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackAuditReviewAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: FallbackAuditReview, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapFallbackAuditReviewAdjuster(var cap: Double) : FallbackAuditReviewAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackAuditReview, value: Double): Double = minOf(value, cap)
}

data class MinimumFallbackAuditReviewAdjuster(var minimum: Double) : FallbackAuditReviewAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackAuditReview, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingFallbackAuditReviewAdjuster(var step: Double) : FallbackAuditReviewAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackAuditReview, value: Double): Double = roundTo(value, step)
}

data class TieredFallbackAuditReviewAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackAuditReviewAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackAuditReview, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the fallback audit review steps in order and keeps a trace of each. */
data class FallbackAuditReviewEngine(
    var steps: MutableList<FallbackAuditReviewAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: FallbackAuditReview): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackAuditReviewAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackAuditReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultFallbackAuditReviewEngine(): FallbackAuditReviewEngine {
    return FallbackAuditReviewEngine(
        steps = mutableListOf<FallbackAuditReviewAdjuster>(
            TitleFallbackAuditReviewAdjuster(
                mutableMapOf<String, Double>("west" to 1.08, "east" to 1.01, "retail" to 0.99),
            ),
            CapFallbackAuditReviewAdjuster(24323.0),
            MinimumFallbackAuditReviewAdjuster(26.3),
            RoundingFallbackAuditReviewAdjuster(0.1),
            TieredFallbackAuditReviewAdjuster(
                mutableListOf<Double>(1746.0, 2334.0, 4282.0),
                mutableListOf<Double>(0.94, 1.14, 1.09),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 76151.1,
    )
}
