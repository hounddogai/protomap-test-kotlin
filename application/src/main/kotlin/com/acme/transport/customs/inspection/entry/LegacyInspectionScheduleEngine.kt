package com.acme.transport.customs.inspection.entry

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the legacy inspection schedule calculation. */
interface LegacyInspectionScheduleStep {
    fun describe(): String
    fun adjust(item: LegacyInspectionSchedule, value: Double): Double
}

data class LegacyInspectionScheduleEndsAtOffsetAdjuster(var weight: Double) : LegacyInspectionScheduleStep {
    override fun describe(): String = "plus ends at times $weight"

    override fun adjust(item: LegacyInspectionSchedule, value: Double): Double {
        return value + item.endsAt.toDouble() * weight
    }
}

data class TieredLegacyInspectionScheduleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyInspectionScheduleStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyInspectionSchedule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentLegacyInspectionScheduleAdjuster(var percent: Double) : LegacyInspectionScheduleStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyInspectionSchedule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumLegacyInspectionScheduleAdjuster(var minimum: Double) : LegacyInspectionScheduleStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyInspectionSchedule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class SegmentLegacyInspectionScheduleAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyInspectionScheduleStep {
    override fun describe(): String = "by segment"

    override fun adjust(item: LegacyInspectionSchedule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedLegacyInspectionScheduleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyInspectionScheduleStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyInspectionSchedule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the legacy inspection schedule steps in order and keeps a trace of each. */
data class LegacyInspectionScheduleEngine(
    var steps: MutableList<LegacyInspectionScheduleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LegacyInspectionSchedule): Double {
        var current = item.effectiveValue()
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
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLegacyInspectionScheduleEngine(flags: Registry): LegacyInspectionScheduleEngine {
    return LegacyInspectionScheduleEngine(
        steps = mutableListOf<LegacyInspectionScheduleStep>(
            LegacyInspectionScheduleEndsAtOffsetAdjuster(0.13),
            TieredLegacyInspectionScheduleAdjuster(
                mutableListOf<Double>(914.0, 6180.0),
                mutableListOf<Double>(0.98, 0.8),
            ),
            PercentLegacyInspectionScheduleAdjuster(-10.7),
            MinimumLegacyInspectionScheduleAdjuster(12.7),
            SegmentLegacyInspectionScheduleAdjuster(mutableMapOf<String, Double>("central" to 1.16)),
            FlaggedLegacyInspectionScheduleAdjuster(flags, "legacy-inspection-schedule-adaptive", -9.2),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 55249.1,
    )
}
