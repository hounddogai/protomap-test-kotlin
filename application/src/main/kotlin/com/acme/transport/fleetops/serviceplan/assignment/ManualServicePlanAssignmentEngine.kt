package com.acme.transport.fleetops.serviceplan.assignment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the manual service plan assignment calculation. */
interface ManualServicePlanAssignmentFactor {
    fun describe(): String
    fun adjust(item: ManualServicePlanAssignment, value: Double): Double
}

data class RoundingManualServicePlanAssignmentAdjuster(var step: Double) : ManualServicePlanAssignmentFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualServicePlanAssignment, value: Double): Double = roundTo(value, step)
}

data class CapManualServicePlanAssignmentAdjuster(var cap: Double) : ManualServicePlanAssignmentFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualServicePlanAssignment, value: Double): Double = minOf(value, cap)
}

data class PercentManualServicePlanAssignmentAdjuster(var percent: Double) : ManualServicePlanAssignmentFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualServicePlanAssignment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedManualServicePlanAssignmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualServicePlanAssignmentFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualServicePlanAssignment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredManualServicePlanAssignmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualServicePlanAssignmentFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualServicePlanAssignment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the manual service plan assignment steps in order and keeps a trace of each. */
data class ManualServicePlanAssignmentEngine(
    var steps: MutableList<ManualServicePlanAssignmentFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ManualServicePlanAssignment): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualServicePlanAssignmentFactor = steps[index]
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

    fun evaluateAll(items: MutableList<ManualServicePlanAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultManualServicePlanAssignmentEngine(flags: Registry): ManualServicePlanAssignmentEngine {
    return ManualServicePlanAssignmentEngine(
        steps = mutableListOf<ManualServicePlanAssignmentFactor>(
            RoundingManualServicePlanAssignmentAdjuster(0.05),
            CapManualServicePlanAssignmentAdjuster(2318.0),
            PercentManualServicePlanAssignmentAdjuster(15.4),
            FlaggedManualServicePlanAssignmentAdjuster(flags, "manual-service-plan-assignment-strict", 0.9),
            TieredManualServicePlanAssignmentAdjuster(
                mutableListOf<Double>(1512.0, 2132.0, 3395.0, 8401.0),
                mutableListOf<Double>(1.04, 1.11, 0.81, 1.11),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 60501.7,
    )
}
