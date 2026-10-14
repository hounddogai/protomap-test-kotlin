package com.acme.transport.fleetops.inspection.inspectiondraft

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the default inspection schedule calculation. */
interface DefaultInspectionScheduleAdjuster {
    fun describe(): String
    fun adjust(item: DefaultInspectionSchedule, value: Double): Double
}

data class CapDefaultInspectionScheduleAdjuster(var cap: Double) : DefaultInspectionScheduleAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultInspectionSchedule, value: Double): Double = minOf(value, cap)
}

data class TieredDefaultInspectionScheduleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultInspectionScheduleAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultInspectionSchedule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumDefaultInspectionScheduleAdjuster(var minimum: Double) : DefaultInspectionScheduleAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultInspectionSchedule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingDefaultInspectionScheduleAdjuster(var step: Double) : DefaultInspectionScheduleAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultInspectionSchedule, value: Double): Double = roundTo(value, step)
}

data class DefaultInspectionScheduleGraceDaysOffsetAdjuster(
    var weight: Double,
) : DefaultInspectionScheduleAdjuster {
    override fun describe(): String = "plus grace days times $weight"

    override fun adjust(item: DefaultInspectionSchedule, value: Double): Double {
        return value + item.graceDays.toDouble() * weight
    }
}

data class FlaggedDefaultInspectionScheduleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultInspectionScheduleAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultInspectionSchedule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the default inspection schedule steps in order and keeps a trace of each. */
data class DefaultInspectionScheduleEngine(
    var steps: MutableList<DefaultInspectionScheduleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DefaultInspectionSchedule): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DefaultInspectionSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDefaultInspectionScheduleEngine(flags: Registry): DefaultInspectionScheduleEngine {
    return DefaultInspectionScheduleEngine(
        steps = mutableListOf<DefaultInspectionScheduleAdjuster>(
            CapDefaultInspectionScheduleAdjuster(11676.0),
            TieredDefaultInspectionScheduleAdjuster(
                mutableListOf<Double>(1493.0, 7003.0, 7792.0, 9825.0),
                mutableListOf<Double>(0.85, 1.1, 1.17, 1.19),
            ),
            MinimumDefaultInspectionScheduleAdjuster(2.5),
            RoundingDefaultInspectionScheduleAdjuster(0.05),
            DefaultInspectionScheduleGraceDaysOffsetAdjuster(0.32),
            FlaggedDefaultInspectionScheduleAdjuster(flags, "default-inspection-schedule-tiered", -8.1),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 36853.9,
    )
}
