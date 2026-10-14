package com.acme.transport.emissions.routeoffset.threshold

import com.acme.transport.kit.flags.Registry

/** One step of the global route offset summary calculation. */
interface GlobalRouteOffsetSummaryModifier {
    fun describe(): String
    fun adjust(item: GlobalRouteOffsetSummary, value: Double): Double
}

data class FlaggedGlobalRouteOffsetSummaryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalRouteOffsetSummaryModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalRouteOffsetSummary, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumGlobalRouteOffsetSummaryAdjuster(var minimum: Double) : GlobalRouteOffsetSummaryModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalRouteOffsetSummary, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapGlobalRouteOffsetSummaryAdjuster(var cap: Double) : GlobalRouteOffsetSummaryModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalRouteOffsetSummary, value: Double): Double = minOf(value, cap)
}

data class LabelGlobalRouteOffsetSummaryAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalRouteOffsetSummaryModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: GlobalRouteOffsetSummary, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredGlobalRouteOffsetSummaryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalRouteOffsetSummaryModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalRouteOffsetSummary, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentGlobalRouteOffsetSummaryAdjuster(var percent: Double) : GlobalRouteOffsetSummaryModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalRouteOffsetSummary, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the global route offset summary steps in order and keeps a trace of each. */
data class GlobalRouteOffsetSummaryEngine(
    var steps: MutableList<GlobalRouteOffsetSummaryModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: GlobalRouteOffsetSummary): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalRouteOffsetSummaryModifier = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalRouteOffsetSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultGlobalRouteOffsetSummaryEngine(flags: Registry): GlobalRouteOffsetSummaryEngine {
    return GlobalRouteOffsetSummaryEngine(
        steps = mutableListOf<GlobalRouteOffsetSummaryModifier>(
            FlaggedGlobalRouteOffsetSummaryAdjuster(flags, "global-route-offset-summary-weighted", 6.6),
            MinimumGlobalRouteOffsetSummaryAdjuster(34.2),
            CapGlobalRouteOffsetSummaryAdjuster(7506.0),
            LabelGlobalRouteOffsetSummaryAdjuster(
                mutableMapOf<String, Double>("north" to 1.29, "online" to 0.93),
            ),
            TieredGlobalRouteOffsetSummaryAdjuster(
                mutableListOf<Double>(2785.0, 3630.0, 6304.0),
                mutableListOf<Double>(0.96, 1.11, 1.06),
            ),
            PercentGlobalRouteOffsetSummaryAdjuster(-14.8),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 68676.7,
    )
}
