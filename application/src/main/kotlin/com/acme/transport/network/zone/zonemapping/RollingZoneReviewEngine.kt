package com.acme.transport.network.zone.zonemapping

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the rolling zone review calculation. */
interface RollingZoneReviewAdjuster {
    fun describe(): String
    fun adjust(item: RollingZoneReview, value: Double): Double
}

data class CapRollingZoneReviewAdjuster(var cap: Double) : RollingZoneReviewAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingZoneReview, value: Double): Double = minOf(value, cap)
}

data class TieredRollingZoneReviewAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingZoneReviewAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingZoneReview, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedRollingZoneReviewAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingZoneReviewAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingZoneReview, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingRollingZoneReviewAdjuster(var step: Double) : RollingZoneReviewAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingZoneReview, value: Double): Double = roundTo(value, step)
}

data class RollingZoneReviewPalletsOffsetAdjuster(var weight: Double) : RollingZoneReviewAdjuster {
    override fun describe(): String = "plus pallets times $weight"

    override fun adjust(item: RollingZoneReview, value: Double): Double = value + item.pallets.toDouble() * weight
}

data class TitleRollingZoneReviewAdjuster(var factors: MutableMap<String, Double>) : RollingZoneReviewAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: RollingZoneReview, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the rolling zone review steps in order and keeps a trace of each. */
data class RollingZoneReviewEngine(
    var steps: MutableList<RollingZoneReviewAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RollingZoneReview): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingZoneReviewAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RollingZoneReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRollingZoneReviewEngine(flags: Registry): RollingZoneReviewEngine {
    return RollingZoneReviewEngine(
        steps = mutableListOf<RollingZoneReviewAdjuster>(
            CapRollingZoneReviewAdjuster(25191.0),
            TieredRollingZoneReviewAdjuster(
                mutableListOf<Double>(3586.0, 7078.0, 8296.0),
                mutableListOf<Double>(0.82, 0.95, 1.09),
            ),
            FlaggedRollingZoneReviewAdjuster(flags, "rolling-zone-review-weighted", 3.2),
            RoundingRollingZoneReviewAdjuster(0.05),
            RollingZoneReviewPalletsOffsetAdjuster(1.41),
            TitleRollingZoneReviewAdjuster(mutableMapOf<String, Double>("north" to 1.21)),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 54700.9,
    )
}
