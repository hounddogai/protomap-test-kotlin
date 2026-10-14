package com.acme.transport.customs.inspection.slot

import com.acme.transport.kit.flags.Registry

/** One step of the regional inspection schedule calculation. */
interface RegionalInspectionScheduleModifier {
    fun describe(): String
    fun adjust(item: RegionalInspectionSchedule, value: Double): Double
}

data class CapRegionalInspectionScheduleAdjuster(var cap: Double) : RegionalInspectionScheduleModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalInspectionSchedule, value: Double): Double = minOf(value, cap)
}

data class FlaggedRegionalInspectionScheduleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalInspectionScheduleModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalInspectionSchedule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentRegionalInspectionScheduleAdjuster(var percent: Double) : RegionalInspectionScheduleModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalInspectionSchedule, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the regional inspection schedule steps in order and keeps a trace of each. */
data class RegionalInspectionScheduleEngine(
    var steps: MutableList<RegionalInspectionScheduleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RegionalInspectionSchedule): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalInspectionScheduleModifier = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalInspectionSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRegionalInspectionScheduleEngine(flags: Registry): RegionalInspectionScheduleEngine {
    return RegionalInspectionScheduleEngine(
        steps = mutableListOf<RegionalInspectionScheduleModifier>(
            CapRegionalInspectionScheduleAdjuster(7354.0),
            FlaggedRegionalInspectionScheduleAdjuster(flags, "regional-inspection-schedule-default", -0.7),
            PercentRegionalInspectionScheduleAdjuster(5.6),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 35217.5,
    )
}
