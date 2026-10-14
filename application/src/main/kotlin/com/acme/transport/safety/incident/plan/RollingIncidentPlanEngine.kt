package com.acme.transport.safety.incident.plan

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the rolling incident plan calculation. */
interface RollingIncidentPlanModifier {
    fun describe(): String
    fun adjust(item: RollingIncidentPlan, value: Double): Double
}

data class FlaggedRollingIncidentPlanAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingIncidentPlanModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingIncidentPlan, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredRollingIncidentPlanAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingIncidentPlanModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingIncidentPlan, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingRollingIncidentPlanAdjuster(var step: Double) : RollingIncidentPlanModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingIncidentPlan, value: Double): Double = roundTo(value, step)
}

data class NameRollingIncidentPlanAdjuster(
    var factors: MutableMap<String, Double>,
) : RollingIncidentPlanModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: RollingIncidentPlan, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapRollingIncidentPlanAdjuster(var cap: Double) : RollingIncidentPlanModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingIncidentPlan, value: Double): Double = minOf(value, cap)
}

/** Runs the rolling incident plan steps in order and keeps a trace of each. */
data class RollingIncidentPlanEngine(
    var steps: MutableList<RollingIncidentPlanModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RollingIncidentPlan): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingIncidentPlanModifier = steps[index]
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

    fun evaluateAll(items: MutableList<RollingIncidentPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRollingIncidentPlanEngine(flags: Registry): RollingIncidentPlanEngine {
    return RollingIncidentPlanEngine(
        steps = mutableListOf<RollingIncidentPlanModifier>(
            FlaggedRollingIncidentPlanAdjuster(flags, "rolling-incident-plan-legacy", 7.3),
            TieredRollingIncidentPlanAdjuster(
                mutableListOf<Double>(2137.0, 5995.0),
                mutableListOf<Double>(1.04, 1.0),
            ),
            RoundingRollingIncidentPlanAdjuster(1.0),
            NameRollingIncidentPlanAdjuster(
                mutableMapOf<String, Double>("east" to 0.73, "central" to 1.37, "west" to 0.98),
            ),
            CapRollingIncidentPlanAdjuster(17309.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 49479.2,
    )
}
