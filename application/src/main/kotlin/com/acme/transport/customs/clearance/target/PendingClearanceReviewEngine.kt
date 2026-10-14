package com.acme.transport.customs.clearance.target

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the pending clearance review calculation. */
interface PendingClearanceReviewFactor {
    fun describe(): String
    fun adjust(item: PendingClearanceReview, value: Double): Double
}

data class TieredPendingClearanceReviewAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingClearanceReviewFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingClearanceReview, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedPendingClearanceReviewAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingClearanceReviewFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingClearanceReview, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TitlePendingClearanceReviewAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingClearanceReviewFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: PendingClearanceReview, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentPendingClearanceReviewAdjuster(var percent: Double) : PendingClearanceReviewFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingClearanceReview, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class PendingClearanceReviewSequenceOffsetAdjuster(var weight: Double) : PendingClearanceReviewFactor {
    override fun describe(): String = "plus sequence times $weight"

    override fun adjust(item: PendingClearanceReview, value: Double): Double {
        return value + item.sequence.toDouble() * weight
    }
}

/** Runs the pending clearance review steps in order and keeps a trace of each. */
data class PendingClearanceReviewEngine(
    var steps: MutableList<PendingClearanceReviewFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PendingClearanceReview): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PendingClearanceReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPendingClearanceReviewEngine(flags: Registry): PendingClearanceReviewEngine {
    return PendingClearanceReviewEngine(
        steps = mutableListOf<PendingClearanceReviewFactor>(
            TieredPendingClearanceReviewAdjuster(
                mutableListOf<Double>(167.0, 1807.0, 2826.0),
                mutableListOf<Double>(0.8, 0.96, 1.11),
            ),
            FlaggedPendingClearanceReviewAdjuster(flags, "pending-clearance-review-strict", 6.6),
            TitlePendingClearanceReviewAdjuster(mutableMapOf<String, Double>("west" to 1.16)),
            PercentPendingClearanceReviewAdjuster(-5.4),
            PendingClearanceReviewSequenceOffsetAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 85662.1,
    )
}
