package com.acme.transport.fleetops.inspection.profile

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the legacy inspection schedule calculation. */
interface LegacyInspectionScheduleModifier {
    fun describe(): String
    fun adjust(item: LegacyInspectionSchedule, value: Double): Double
}

data class FlaggedLegacyInspectionScheduleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyInspectionScheduleModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyInspectionSchedule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapLegacyInspectionScheduleAdjuster(var cap: Double) : LegacyInspectionScheduleModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyInspectionSchedule, value: Double): Double = minOf(value, cap)
}

data class UnitLegacyInspectionScheduleAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyInspectionScheduleModifier {
    override fun describe(): String = "by unit"

    override fun adjust(item: LegacyInspectionSchedule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumLegacyInspectionScheduleAdjuster(var minimum: Double) : LegacyInspectionScheduleModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyInspectionSchedule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingLegacyInspectionScheduleAdjuster(var step: Double) : LegacyInspectionScheduleModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyInspectionSchedule, value: Double): Double = roundTo(value, step)
}

/** Runs the legacy inspection schedule steps in order and keeps a trace of each. */
data class LegacyInspectionScheduleEngine(
    var steps: MutableList<LegacyInspectionScheduleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LegacyInspectionSchedule): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LegacyInspectionSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLegacyInspectionScheduleEngine(flags: Registry): LegacyInspectionScheduleEngine {
    return LegacyInspectionScheduleEngine(
        steps = mutableListOf<LegacyInspectionScheduleModifier>(
            FlaggedLegacyInspectionScheduleAdjuster(flags, "legacy-inspection-schedule-rolling", 3.8),
            CapLegacyInspectionScheduleAdjuster(42903.0),
            UnitLegacyInspectionScheduleAdjuster(mutableMapOf<String, Double>("east" to 0.89, "retail" to 1.37)),
            MinimumLegacyInspectionScheduleAdjuster(47.6),
            RoundingLegacyInspectionScheduleAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 84485.2,
    )
}
