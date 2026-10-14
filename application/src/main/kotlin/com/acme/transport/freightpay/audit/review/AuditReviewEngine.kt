package com.acme.transport.freightpay.audit.review

import com.acme.transport.kit.flags.Registry

/** One step of the audit review calculation. */
interface AuditReviewFactor {
    fun describe(): String
    fun adjust(item: AuditReview, value: Double): Double
}

data class FlaggedAuditReviewAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AuditReviewFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AuditReview, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentAuditReviewAdjuster(var percent: Double) : AuditReviewFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AuditReview, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapAuditReviewAdjuster(var cap: Double) : AuditReviewFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AuditReview, value: Double): Double = minOf(value, cap)
}

data class MinimumAuditReviewAdjuster(var minimum: Double) : AuditReviewFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AuditReview, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the audit review steps in order and keeps a trace of each. */
data class AuditReviewEngine(
    var steps: MutableList<AuditReviewFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AuditReview): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AuditReviewFactor = steps[index]
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

    fun evaluateAll(items: MutableList<AuditReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAuditReviewEngine(flags: Registry): AuditReviewEngine {
    return AuditReviewEngine(
        steps = mutableListOf<AuditReviewFactor>(
            FlaggedAuditReviewAdjuster(flags, "audit-review-regional", -0.8),
            PercentAuditReviewAdjuster(-13.2),
            CapAuditReviewAdjuster(9209.0),
            MinimumAuditReviewAdjuster(15.6),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 92897.6,
    )
}
