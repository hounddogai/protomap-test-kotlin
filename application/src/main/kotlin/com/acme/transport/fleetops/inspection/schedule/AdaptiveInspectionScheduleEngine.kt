package com.acme.transport.fleetops.inspection.schedule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the adaptive inspection schedule calculation. */
interface AdaptiveInspectionScheduleStep {
    fun describe(): String
    fun adjust(item: AdaptiveInspectionSchedule, value: Double): Double
}

data class TieredAdaptiveInspectionScheduleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveInspectionScheduleStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveInspectionSchedule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedAdaptiveInspectionScheduleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveInspectionScheduleStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveInspectionSchedule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentAdaptiveInspectionScheduleAdjuster(var percent: Double) : AdaptiveInspectionScheduleStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveInspectionSchedule, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the adaptive inspection schedule steps in order and keeps a trace of each. */
data class AdaptiveInspectionScheduleEngine(
    var steps: MutableList<AdaptiveInspectionScheduleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AdaptiveInspectionSchedule): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AdaptiveInspectionSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAdaptiveInspectionScheduleEngine(flags: Registry): AdaptiveInspectionScheduleEngine {
    return AdaptiveInspectionScheduleEngine(
        steps = mutableListOf<AdaptiveInspectionScheduleStep>(
            TieredAdaptiveInspectionScheduleAdjuster(
                mutableListOf<Double>(6023.0, 6198.0, 7422.0),
                mutableListOf<Double>(1.13, 1.14, 1.13),
            ),
            FlaggedAdaptiveInspectionScheduleAdjuster(flags, "adaptive-inspection-schedule-rolling", -1.5),
            PercentAdaptiveInspectionScheduleAdjuster(-8.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 53440.6,
    )
}
