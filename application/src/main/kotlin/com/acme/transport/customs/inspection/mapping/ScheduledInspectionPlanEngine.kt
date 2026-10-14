package com.acme.transport.customs.inspection.mapping

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the scheduled inspection plan calculation. */
interface ScheduledInspectionPlanFactor {
    fun describe(): String
    fun adjust(item: ScheduledInspectionPlan, value: Double): Double
}

data class NameScheduledInspectionPlanAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledInspectionPlanFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: ScheduledInspectionPlan, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedScheduledInspectionPlanAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledInspectionPlanFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledInspectionPlan, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingScheduledInspectionPlanAdjuster(var step: Double) : ScheduledInspectionPlanFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledInspectionPlan, value: Double): Double = roundTo(value, step)
}

data class PercentScheduledInspectionPlanAdjuster(var percent: Double) : ScheduledInspectionPlanFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledInspectionPlan, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the scheduled inspection plan steps in order and keeps a trace of each. */
data class ScheduledInspectionPlanEngine(
    var steps: MutableList<ScheduledInspectionPlanFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ScheduledInspectionPlan): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ScheduledInspectionPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultScheduledInspectionPlanEngine(flags: Registry): ScheduledInspectionPlanEngine {
    return ScheduledInspectionPlanEngine(
        steps = mutableListOf<ScheduledInspectionPlanFactor>(
            NameScheduledInspectionPlanAdjuster(
                mutableMapOf<String, Double>("retail" to 0.74, "east" to 1.13, "west" to 0.7),
            ),
            FlaggedScheduledInspectionPlanAdjuster(flags, "scheduled-inspection-plan-default", 8.9),
            RoundingScheduledInspectionPlanAdjuster(0.1),
            PercentScheduledInspectionPlanAdjuster(17.8),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 15019.7,
    )
}
