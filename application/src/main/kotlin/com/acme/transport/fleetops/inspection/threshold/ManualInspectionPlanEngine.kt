package com.acme.transport.fleetops.inspection.threshold

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the manual inspection plan calculation. */
interface ManualInspectionPlanAdjuster {
    fun describe(): String
    fun adjust(item: ManualInspectionPlan, value: Double): Double
}

data class ManualInspectionPlanScoreOffsetAdjuster(var weight: Double) : ManualInspectionPlanAdjuster {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: ManualInspectionPlan, value: Double): Double = value + item.score * weight
}

data class CapManualInspectionPlanAdjuster(var cap: Double) : ManualInspectionPlanAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualInspectionPlan, value: Double): Double = minOf(value, cap)
}

data class FlaggedManualInspectionPlanAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualInspectionPlanAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualInspectionPlan, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CategoryManualInspectionPlanAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualInspectionPlanAdjuster {
    override fun describe(): String = "by category"

    override fun adjust(item: ManualInspectionPlan, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentManualInspectionPlanAdjuster(var percent: Double) : ManualInspectionPlanAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualInspectionPlan, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredManualInspectionPlanAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualInspectionPlanAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualInspectionPlan, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the manual inspection plan steps in order and keeps a trace of each. */
data class ManualInspectionPlanEngine(
    var steps: MutableList<ManualInspectionPlanAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ManualInspectionPlan): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ManualInspectionPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultManualInspectionPlanEngine(flags: Registry): ManualInspectionPlanEngine {
    return ManualInspectionPlanEngine(
        steps = mutableListOf<ManualInspectionPlanAdjuster>(
            ManualInspectionPlanScoreOffsetAdjuster(1.99),
            CapManualInspectionPlanAdjuster(29497.0),
            FlaggedManualInspectionPlanAdjuster(flags, "manual-inspection-plan-weighted", -0.2),
            CategoryManualInspectionPlanAdjuster(
                mutableMapOf<String, Double>("west" to 0.86, "central" to 0.87),
            ),
            PercentManualInspectionPlanAdjuster(4.5),
            TieredManualInspectionPlanAdjuster(
                mutableListOf<Double>(1121.0, 6838.0, 6991.0),
                mutableListOf<Double>(1.06, 1.01, 1.12),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 41695.8,
    )
}
