package com.acme.transport.linehaul.trailer.trailermilestone

import com.acme.transport.kit.flags.Registry

/** One step of the manual trailer milestone calculation. */
interface ManualTrailerMilestoneAdjuster {
    fun describe(): String
    fun adjust(item: ManualTrailerMilestone, value: Double): Double
}

data class PercentManualTrailerMilestoneAdjuster(var percent: Double) : ManualTrailerMilestoneAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualTrailerMilestone, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredManualTrailerMilestoneAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualTrailerMilestoneAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualTrailerMilestone, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumManualTrailerMilestoneAdjuster(var minimum: Double) : ManualTrailerMilestoneAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualTrailerMilestone, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedManualTrailerMilestoneAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualTrailerMilestoneAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualTrailerMilestone, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class ManualTrailerMilestoneBudgetOffsetAdjuster(var weight: Double) : ManualTrailerMilestoneAdjuster {
    override fun describe(): String = "plus budget times $weight"

    override fun adjust(item: ManualTrailerMilestone, value: Double): Double = value + item.budget * weight
}

data class CapManualTrailerMilestoneAdjuster(var cap: Double) : ManualTrailerMilestoneAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualTrailerMilestone, value: Double): Double = minOf(value, cap)
}

/** Runs the manual trailer milestone steps in order and keeps a trace of each. */
data class ManualTrailerMilestoneEngine(
    var steps: MutableList<ManualTrailerMilestoneAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ManualTrailerMilestone): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualTrailerMilestoneAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ManualTrailerMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultManualTrailerMilestoneEngine(flags: Registry): ManualTrailerMilestoneEngine {
    return ManualTrailerMilestoneEngine(
        steps = mutableListOf<ManualTrailerMilestoneAdjuster>(
            PercentManualTrailerMilestoneAdjuster(5.3),
            TieredManualTrailerMilestoneAdjuster(
                mutableListOf<Double>(8212.0, 8991.0),
                mutableListOf<Double>(0.98, 0.93),
            ),
            MinimumManualTrailerMilestoneAdjuster(46.5),
            FlaggedManualTrailerMilestoneAdjuster(flags, "manual-trailer-milestone-fallback", -0.3),
            ManualTrailerMilestoneBudgetOffsetAdjuster(0.68),
            CapManualTrailerMilestoneAdjuster(14014.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 94512.1,
    )
}
