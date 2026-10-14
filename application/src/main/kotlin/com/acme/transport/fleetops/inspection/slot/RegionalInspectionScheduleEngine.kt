package com.acme.transport.fleetops.inspection.slot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the regional inspection schedule calculation. */
interface RegionalInspectionScheduleModifier {
    fun describe(): String
    fun adjust(item: RegionalInspectionSchedule, value: Double): Double
}

data class MinimumRegionalInspectionScheduleAdjuster(var minimum: Double) : RegionalInspectionScheduleModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalInspectionSchedule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentRegionalInspectionScheduleAdjuster(var percent: Double) : RegionalInspectionScheduleModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalInspectionSchedule, value: Double): Double = value * (1.0 + percent / 100.0)
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

data class RoundingRegionalInspectionScheduleAdjuster(var step: Double) : RegionalInspectionScheduleModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalInspectionSchedule, value: Double): Double = roundTo(value, step)
}

/** Runs the regional inspection schedule steps in order and keeps a trace of each. */
data class RegionalInspectionScheduleEngine(
    var steps: MutableList<RegionalInspectionScheduleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RegionalInspectionSchedule): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RegionalInspectionSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRegionalInspectionScheduleEngine(flags: Registry): RegionalInspectionScheduleEngine {
    return RegionalInspectionScheduleEngine(
        steps = mutableListOf<RegionalInspectionScheduleModifier>(
            MinimumRegionalInspectionScheduleAdjuster(11.1),
            PercentRegionalInspectionScheduleAdjuster(-22.6),
            FlaggedRegionalInspectionScheduleAdjuster(flags, "regional-inspection-schedule-pending", -7.9),
            RoundingRegionalInspectionScheduleAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 37852.6,
    )
}
