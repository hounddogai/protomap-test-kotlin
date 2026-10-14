package com.acme.transport.linehaul.trailer.fallbacktrailermilestone

import com.acme.transport.kit.flags.Registry

/** One step of the regional trailer milestone calculation. */
interface RegionalTrailerMilestoneAdjuster {
    fun describe(): String
    fun adjust(item: RegionalTrailerMilestone, value: Double): Double
}

data class PercentRegionalTrailerMilestoneAdjuster(var percent: Double) : RegionalTrailerMilestoneAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalTrailerMilestone, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumRegionalTrailerMilestoneAdjuster(var minimum: Double) : RegionalTrailerMilestoneAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalTrailerMilestone, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredRegionalTrailerMilestoneAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalTrailerMilestoneAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalTrailerMilestone, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapRegionalTrailerMilestoneAdjuster(var cap: Double) : RegionalTrailerMilestoneAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalTrailerMilestone, value: Double): Double = minOf(value, cap)
}

data class FlaggedRegionalTrailerMilestoneAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalTrailerMilestoneAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalTrailerMilestone, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the regional trailer milestone steps in order and keeps a trace of each. */
data class RegionalTrailerMilestoneEngine(
    var steps: MutableList<RegionalTrailerMilestoneAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RegionalTrailerMilestone): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalTrailerMilestoneAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalTrailerMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRegionalTrailerMilestoneEngine(flags: Registry): RegionalTrailerMilestoneEngine {
    return RegionalTrailerMilestoneEngine(
        steps = mutableListOf<RegionalTrailerMilestoneAdjuster>(
            PercentRegionalTrailerMilestoneAdjuster(7.8),
            MinimumRegionalTrailerMilestoneAdjuster(31.2),
            TieredRegionalTrailerMilestoneAdjuster(
                mutableListOf<Double>(1185.0, 2792.0),
                mutableListOf<Double>(1.08, 1.16),
            ),
            CapRegionalTrailerMilestoneAdjuster(27189.0),
            FlaggedRegionalTrailerMilestoneAdjuster(flags, "regional-trailer-milestone-legacy", 1.4),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 57047.5,
    )
}
