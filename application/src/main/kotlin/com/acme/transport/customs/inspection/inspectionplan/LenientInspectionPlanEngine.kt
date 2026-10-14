package com.acme.transport.customs.inspection.inspectionplan

import com.acme.transport.kit.flags.Registry

/** One step of the lenient inspection plan calculation. */
interface LenientInspectionPlanAdjuster {
    fun describe(): String
    fun adjust(item: LenientInspectionPlan, value: Double): Double
}

data class PercentLenientInspectionPlanAdjuster(var percent: Double) : LenientInspectionPlanAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientInspectionPlan, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapLenientInspectionPlanAdjuster(var cap: Double) : LenientInspectionPlanAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientInspectionPlan, value: Double): Double = minOf(value, cap)
}

data class FlaggedLenientInspectionPlanAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientInspectionPlanAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientInspectionPlan, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the lenient inspection plan steps in order and keeps a trace of each. */
data class LenientInspectionPlanEngine(
    var steps: MutableList<LenientInspectionPlanAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LenientInspectionPlan): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LenientInspectionPlanAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<LenientInspectionPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLenientInspectionPlanEngine(flags: Registry): LenientInspectionPlanEngine {
    return LenientInspectionPlanEngine(
        steps = mutableListOf<LenientInspectionPlanAdjuster>(
            PercentLenientInspectionPlanAdjuster(-24.2),
            CapLenientInspectionPlanAdjuster(36441.0),
            FlaggedLenientInspectionPlanAdjuster(flags, "lenient-inspection-plan-global", 9.8),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 39096.2,
    )
}
