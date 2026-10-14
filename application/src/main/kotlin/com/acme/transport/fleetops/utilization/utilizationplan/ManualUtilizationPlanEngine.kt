package com.acme.transport.fleetops.utilization.utilizationplan

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the manual utilization plan calculation. */
interface ManualUtilizationPlanFactor {
    fun describe(): String
    fun adjust(item: ManualUtilizationPlan, value: Double): Double
}

data class RoundingManualUtilizationPlanAdjuster(var step: Double) : ManualUtilizationPlanFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualUtilizationPlan, value: Double): Double = roundTo(value, step)
}

data class FlaggedManualUtilizationPlanAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualUtilizationPlanFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualUtilizationPlan, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class NameManualUtilizationPlanAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualUtilizationPlanFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: ManualUtilizationPlan, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the manual utilization plan steps in order and keeps a trace of each. */
data class ManualUtilizationPlanEngine(
    var steps: MutableList<ManualUtilizationPlanFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ManualUtilizationPlan): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualUtilizationPlanFactor = steps[index]
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

    fun evaluateAll(items: MutableList<ManualUtilizationPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultManualUtilizationPlanEngine(flags: Registry): ManualUtilizationPlanEngine {
    return ManualUtilizationPlanEngine(
        steps = mutableListOf<ManualUtilizationPlanFactor>(
            RoundingManualUtilizationPlanAdjuster(0.1),
            FlaggedManualUtilizationPlanAdjuster(flags, "manual-utilization-plan-rolling", 7.8),
            NameManualUtilizationPlanAdjuster(mutableMapOf<String, Double>("retail" to 0.91)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 94227.6,
    )
}
