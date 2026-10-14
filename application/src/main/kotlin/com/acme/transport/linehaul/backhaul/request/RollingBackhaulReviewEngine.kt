package com.acme.transport.linehaul.backhaul.request

import com.acme.transport.kit.flags.Registry

/** One step of the rolling backhaul review calculation. */
interface RollingBackhaulReviewAdjuster {
    fun describe(): String
    fun adjust(item: RollingBackhaulReview, value: Double): Double
}

data class RollingBackhaulReviewVersionOffsetAdjuster(var weight: Double) : RollingBackhaulReviewAdjuster {
    override fun describe(): String = "plus version times $weight"

    override fun adjust(item: RollingBackhaulReview, value: Double): Double = value + item.version.toDouble() * weight
}

data class CategoryRollingBackhaulReviewAdjuster(
    var factors: MutableMap<String, Double>,
) : RollingBackhaulReviewAdjuster {
    override fun describe(): String = "by category"

    override fun adjust(item: RollingBackhaulReview, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapRollingBackhaulReviewAdjuster(var cap: Double) : RollingBackhaulReviewAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingBackhaulReview, value: Double): Double = minOf(value, cap)
}

data class FlaggedRollingBackhaulReviewAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingBackhaulReviewAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingBackhaulReview, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the rolling backhaul review steps in order and keeps a trace of each. */
data class RollingBackhaulReviewEngine(
    var steps: MutableList<RollingBackhaulReviewAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RollingBackhaulReview): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingBackhaulReviewAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RollingBackhaulReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRollingBackhaulReviewEngine(flags: Registry): RollingBackhaulReviewEngine {
    return RollingBackhaulReviewEngine(
        steps = mutableListOf<RollingBackhaulReviewAdjuster>(
            RollingBackhaulReviewVersionOffsetAdjuster(0.25),
            CategoryRollingBackhaulReviewAdjuster(
                mutableMapOf<String, Double>("east" to 1.02, "north" to 1.28, "south" to 0.85),
            ),
            CapRollingBackhaulReviewAdjuster(36548.0),
            FlaggedRollingBackhaulReviewAdjuster(flags, "rolling-backhaul-review-tiered", -4.5),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 90986.8,
    )
}
