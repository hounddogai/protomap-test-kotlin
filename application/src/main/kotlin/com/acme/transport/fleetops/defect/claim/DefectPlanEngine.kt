package com.acme.transport.fleetops.defect.claim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the defect plan calculation. */
interface DefectPlanFactor {
    fun describe(): String
    fun adjust(item: DefectPlan, value: Double): Double
}

data class RoundingDefectPlanAdjuster(var step: Double) : DefectPlanFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefectPlan, value: Double): Double = roundTo(value, step)
}

data class DefectPlanEndsAtOffsetAdjuster(var weight: Double) : DefectPlanFactor {
    override fun describe(): String = "plus ends at times $weight"

    override fun adjust(item: DefectPlan, value: Double): Double = value + item.endsAt.toDouble() * weight
}

data class MinimumDefectPlanAdjuster(var minimum: Double) : DefectPlanFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefectPlan, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentDefectPlanAdjuster(var percent: Double) : DefectPlanFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefectPlan, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapDefectPlanAdjuster(var cap: Double) : DefectPlanFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefectPlan, value: Double): Double = minOf(value, cap)
}

data class FlaggedDefectPlanAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefectPlanFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefectPlan, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the defect plan steps in order and keeps a trace of each. */
data class DefectPlanEngine(
    var steps: MutableList<DefectPlanFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DefectPlan): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefectPlanFactor = steps[index]
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

    fun evaluateAll(items: MutableList<DefectPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDefectPlanEngine(flags: Registry): DefectPlanEngine {
    return DefectPlanEngine(
        steps = mutableListOf<DefectPlanFactor>(
            RoundingDefectPlanAdjuster(5.0),
            DefectPlanEndsAtOffsetAdjuster(0.28),
            MinimumDefectPlanAdjuster(4.2),
            PercentDefectPlanAdjuster(21.0),
            CapDefectPlanAdjuster(39634.0),
            FlaggedDefectPlanAdjuster(flags, "defect-plan-batched", -3.6),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 88887.5,
    )
}
