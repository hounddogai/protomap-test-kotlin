package com.acme.transport.freightpay.audit.auditreview

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the strict audit review calculation. */
interface StrictAuditReviewStep {
    fun describe(): String
    fun adjust(item: StrictAuditReview, value: Double): Double
}

data class SourceStrictAuditReviewAdjuster(var factors: MutableMap<String, Double>) : StrictAuditReviewStep {
    override fun describe(): String = "by source"

    override fun adjust(item: StrictAuditReview, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedStrictAuditReviewAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictAuditReviewStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictAuditReview, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class StrictAuditReviewWindowDaysOffsetAdjuster(var weight: Double) : StrictAuditReviewStep {
    override fun describe(): String = "plus window days times $weight"

    override fun adjust(item: StrictAuditReview, value: Double): Double = value + item.windowDays.toDouble() * weight
}

data class PercentStrictAuditReviewAdjuster(var percent: Double) : StrictAuditReviewStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictAuditReview, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapStrictAuditReviewAdjuster(var cap: Double) : StrictAuditReviewStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictAuditReview, value: Double): Double = minOf(value, cap)
}

data class MinimumStrictAuditReviewAdjuster(var minimum: Double) : StrictAuditReviewStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictAuditReview, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the strict audit review steps in order and keeps a trace of each. */
data class StrictAuditReviewEngine(
    var steps: MutableList<StrictAuditReviewStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: StrictAuditReview): Double {
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

    fun evaluateAll(items: MutableList<StrictAuditReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultStrictAuditReviewEngine(flags: Registry): StrictAuditReviewEngine {
    return StrictAuditReviewEngine(
        steps = mutableListOf<StrictAuditReviewStep>(
            SourceStrictAuditReviewAdjuster(
                mutableMapOf<String, Double>("retail" to 0.79, "online" to 1.06, "west" to 1.17),
            ),
            FlaggedStrictAuditReviewAdjuster(flags, "strict-audit-review-tiered", -1.8),
            StrictAuditReviewWindowDaysOffsetAdjuster(0.83),
            PercentStrictAuditReviewAdjuster(-23.9),
            CapStrictAuditReviewAdjuster(35698.0),
            MinimumStrictAuditReviewAdjuster(31.1),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 31818.6,
    )
}
