package com.acme.transport.terminal.traileryard.audit

import com.acme.transport.kit.flags.Registry

/** One step of the regional trailer yard plan calculation. */
interface RegionalTrailerYardPlanAdjuster {
    fun describe(): String
    fun adjust(item: RegionalTrailerYardPlan, value: Double): Double
}

data class CapRegionalTrailerYardPlanAdjuster(var cap: Double) : RegionalTrailerYardPlanAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalTrailerYardPlan, value: Double): Double = minOf(value, cap)
}

data class TieredRegionalTrailerYardPlanAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalTrailerYardPlanAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalTrailerYardPlan, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentRegionalTrailerYardPlanAdjuster(var percent: Double) : RegionalTrailerYardPlanAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalTrailerYardPlan, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedRegionalTrailerYardPlanAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalTrailerYardPlanAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalTrailerYardPlan, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the regional trailer yard plan steps in order and keeps a trace of each. */
data class RegionalTrailerYardPlanEngine(
    var steps: MutableList<RegionalTrailerYardPlanAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RegionalTrailerYardPlan): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalTrailerYardPlanAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalTrailerYardPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRegionalTrailerYardPlanEngine(flags: Registry): RegionalTrailerYardPlanEngine {
    return RegionalTrailerYardPlanEngine(
        steps = mutableListOf<RegionalTrailerYardPlanAdjuster>(
            CapRegionalTrailerYardPlanAdjuster(41989.0),
            TieredRegionalTrailerYardPlanAdjuster(
                mutableListOf<Double>(2004.0, 6077.0, 6769.0),
                mutableListOf<Double>(1.14, 1.14, 1.12),
            ),
            PercentRegionalTrailerYardPlanAdjuster(-13.5),
            FlaggedRegionalTrailerYardPlanAdjuster(flags, "regional-trailer-yard-plan-strict", 7.5),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 70793.1,
    )
}
