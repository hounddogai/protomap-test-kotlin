package com.acme.transport.safety.violation.proposal

import com.acme.transport.kit.flags.Registry

/** One step of the violation review calculation. */
interface ViolationReviewModifier {
    fun describe(): String
    fun adjust(item: ViolationReview, value: Double): Double
}

data class FlaggedViolationReviewAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ViolationReviewModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ViolationReview, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredViolationReviewAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ViolationReviewModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ViolationReview, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class ViolationReviewCreatedAtOffsetAdjuster(var weight: Double) : ViolationReviewModifier {
    override fun describe(): String = "plus created at times $weight"

    override fun adjust(item: ViolationReview, value: Double): Double = value + item.createdAt.toDouble() * weight
}

data class CapViolationReviewAdjuster(var cap: Double) : ViolationReviewModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ViolationReview, value: Double): Double = minOf(value, cap)
}

/** Runs the violation review steps in order and keeps a trace of each. */
data class ViolationReviewEngine(
    var steps: MutableList<ViolationReviewModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ViolationReview): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ViolationReviewModifier = steps[index]
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

    fun evaluateAll(items: MutableList<ViolationReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultViolationReviewEngine(flags: Registry): ViolationReviewEngine {
    return ViolationReviewEngine(
        steps = mutableListOf<ViolationReviewModifier>(
            FlaggedViolationReviewAdjuster(flags, "violation-review-fallback", 5.2),
            TieredViolationReviewAdjuster(
                mutableListOf<Double>(17.0, 4843.0, 6831.0, 7648.0),
                mutableListOf<Double>(0.88, 1.09, 1.12, 1.1),
            ),
            ViolationReviewCreatedAtOffsetAdjuster(0.35),
            CapViolationReviewAdjuster(22096.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 38032.7,
    )
}
