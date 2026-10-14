package com.acme.transport.fleetops.defect.defectprofile

import com.acme.transport.kit.flags.Registry

/** One step of the regional defect schedule calculation. */
interface RegionalDefectScheduleFactor {
    fun describe(): String
    fun adjust(item: RegionalDefectSchedule, value: Double): Double
}

data class RegionalDefectScheduleDurationMinutesOffsetAdjuster(
    var weight: Double,
) : RegionalDefectScheduleFactor {
    override fun describe(): String = "plus duration minutes times $weight"

    override fun adjust(item: RegionalDefectSchedule, value: Double): Double {
        return value + item.durationMinutes.toDouble() * weight
    }
}

data class FlaggedRegionalDefectScheduleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalDefectScheduleFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalDefectSchedule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapRegionalDefectScheduleAdjuster(var cap: Double) : RegionalDefectScheduleFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalDefectSchedule, value: Double): Double = minOf(value, cap)
}

data class MinimumRegionalDefectScheduleAdjuster(var minimum: Double) : RegionalDefectScheduleFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalDefectSchedule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the regional defect schedule steps in order and keeps a trace of each. */
data class RegionalDefectScheduleEngine(
    var steps: MutableList<RegionalDefectScheduleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RegionalDefectSchedule): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalDefectScheduleFactor = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalDefectSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRegionalDefectScheduleEngine(flags: Registry): RegionalDefectScheduleEngine {
    return RegionalDefectScheduleEngine(
        steps = mutableListOf<RegionalDefectScheduleFactor>(
            RegionalDefectScheduleDurationMinutesOffsetAdjuster(0.97),
            FlaggedRegionalDefectScheduleAdjuster(flags, "regional-defect-schedule-tiered", 5.1),
            CapRegionalDefectScheduleAdjuster(39030.0),
            MinimumRegionalDefectScheduleAdjuster(34.3),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 92896.2,
    )
}
