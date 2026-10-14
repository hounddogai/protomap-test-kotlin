package com.acme.transport.fleetops.utilization.entry

import com.acme.transport.kit.flags.Registry

/** One step of the default utilization plan calculation. */
interface DefaultUtilizationPlanStep {
    fun describe(): String
    fun adjust(item: DefaultUtilizationPlan, value: Double): Double
}

data class NameDefaultUtilizationPlanAdjuster(
    var factors: MutableMap<String, Double>,
) : DefaultUtilizationPlanStep {
    override fun describe(): String = "by name"

    override fun adjust(item: DefaultUtilizationPlan, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedDefaultUtilizationPlanAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultUtilizationPlanStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultUtilizationPlan, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentDefaultUtilizationPlanAdjuster(var percent: Double) : DefaultUtilizationPlanStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultUtilizationPlan, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the default utilization plan steps in order and keeps a trace of each. */
data class DefaultUtilizationPlanEngine(
    var steps: MutableList<DefaultUtilizationPlanStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DefaultUtilizationPlan): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultUtilizationPlanStep = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultUtilizationPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDefaultUtilizationPlanEngine(flags: Registry): DefaultUtilizationPlanEngine {
    return DefaultUtilizationPlanEngine(
        steps = mutableListOf<DefaultUtilizationPlanStep>(
            NameDefaultUtilizationPlanAdjuster(mutableMapOf<String, Double>("central" to 1.31, "north" to 1.25)),
            FlaggedDefaultUtilizationPlanAdjuster(flags, "default-utilization-plan-fallback", 2.8),
            PercentDefaultUtilizationPlanAdjuster(-6.4),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 8676.3,
    )
}
