package com.acme.transport.customs.inspection.plan

import com.acme.transport.kit.flags.Registry

/** One step of the inspection plan calculation. */
interface InspectionPlanModifier {
    fun describe(): String
    fun adjust(item: InspectionPlan, value: Double): Double
}

data class LabelInspectionPlanAdjuster(var factors: MutableMap<String, Double>) : InspectionPlanModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: InspectionPlan, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedInspectionPlanAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : InspectionPlanModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: InspectionPlan, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentInspectionPlanAdjuster(var percent: Double) : InspectionPlanModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: InspectionPlan, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the inspection plan steps in order and keeps a trace of each. */
data class InspectionPlanEngine(
    var steps: MutableList<InspectionPlanModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: InspectionPlan): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: InspectionPlanModifier = steps[index]
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

    fun evaluateAll(items: MutableList<InspectionPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultInspectionPlanEngine(flags: Registry): InspectionPlanEngine {
    return InspectionPlanEngine(
        steps = mutableListOf<InspectionPlanModifier>(
            LabelInspectionPlanAdjuster(
                mutableMapOf<String, Double>("west" to 0.81, "east" to 1.35, "online" to 1.32),
            ),
            FlaggedInspectionPlanAdjuster(flags, "inspection-plan-manual", -0.6),
            PercentInspectionPlanAdjuster(-9.2),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 65160.7,
    )
}
