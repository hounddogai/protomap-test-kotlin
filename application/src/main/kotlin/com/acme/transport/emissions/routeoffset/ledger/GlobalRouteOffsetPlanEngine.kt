package com.acme.transport.emissions.routeoffset.ledger

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the global route offset plan calculation. */
interface GlobalRouteOffsetPlanModifier {
    fun describe(): String
    fun adjust(item: GlobalRouteOffsetPlan, value: Double): Double
}

data class GlobalRouteOffsetPlanVersionOffsetAdjuster(var weight: Double) : GlobalRouteOffsetPlanModifier {
    override fun describe(): String = "plus version times $weight"

    override fun adjust(item: GlobalRouteOffsetPlan, value: Double): Double = value + item.version.toDouble() * weight
}

data class SourceGlobalRouteOffsetPlanAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalRouteOffsetPlanModifier {
    override fun describe(): String = "by source"

    override fun adjust(item: GlobalRouteOffsetPlan, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedGlobalRouteOffsetPlanAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalRouteOffsetPlanModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalRouteOffsetPlan, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumGlobalRouteOffsetPlanAdjuster(var minimum: Double) : GlobalRouteOffsetPlanModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalRouteOffsetPlan, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingGlobalRouteOffsetPlanAdjuster(var step: Double) : GlobalRouteOffsetPlanModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalRouteOffsetPlan, value: Double): Double = roundTo(value, step)
}

data class CapGlobalRouteOffsetPlanAdjuster(var cap: Double) : GlobalRouteOffsetPlanModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalRouteOffsetPlan, value: Double): Double = minOf(value, cap)
}

/** Runs the global route offset plan steps in order and keeps a trace of each. */
data class GlobalRouteOffsetPlanEngine(
    var steps: MutableList<GlobalRouteOffsetPlanModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: GlobalRouteOffsetPlan): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalRouteOffsetPlanModifier = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalRouteOffsetPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultGlobalRouteOffsetPlanEngine(flags: Registry): GlobalRouteOffsetPlanEngine {
    return GlobalRouteOffsetPlanEngine(
        steps = mutableListOf<GlobalRouteOffsetPlanModifier>(
            GlobalRouteOffsetPlanVersionOffsetAdjuster(1.78),
            SourceGlobalRouteOffsetPlanAdjuster(mutableMapOf<String, Double>("east" to 1.26, "retail" to 1.29)),
            FlaggedGlobalRouteOffsetPlanAdjuster(flags, "global-route-offset-plan-batched", -9.9),
            MinimumGlobalRouteOffsetPlanAdjuster(20.5),
            RoundingGlobalRouteOffsetPlanAdjuster(0.05),
            CapGlobalRouteOffsetPlanAdjuster(46255.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 26873.2,
    )
}
