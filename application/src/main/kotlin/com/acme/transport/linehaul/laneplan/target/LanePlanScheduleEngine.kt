package com.acme.transport.linehaul.laneplan.target

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the lane plan schedule calculation. */
interface LanePlanScheduleFactor {
    fun describe(): String
    fun adjust(item: LanePlanSchedule, value: Double): Double
}

data class CapLanePlanScheduleAdjuster(var cap: Double) : LanePlanScheduleFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LanePlanSchedule, value: Double): Double = minOf(value, cap)
}

data class PercentLanePlanScheduleAdjuster(var percent: Double) : LanePlanScheduleFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LanePlanSchedule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingLanePlanScheduleAdjuster(var step: Double) : LanePlanScheduleFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LanePlanSchedule, value: Double): Double = roundTo(value, step)
}

data class FlaggedLanePlanScheduleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LanePlanScheduleFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LanePlanSchedule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the lane plan schedule steps in order and keeps a trace of each. */
data class LanePlanScheduleEngine(
    var steps: MutableList<LanePlanScheduleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LanePlanSchedule): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LanePlanSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLanePlanScheduleEngine(flags: Registry): LanePlanScheduleEngine {
    return LanePlanScheduleEngine(
        steps = mutableListOf<LanePlanScheduleFactor>(
            CapLanePlanScheduleAdjuster(46682.0),
            PercentLanePlanScheduleAdjuster(6.7),
            RoundingLanePlanScheduleAdjuster(0.25),
            FlaggedLanePlanScheduleAdjuster(flags, "lane-plan-schedule-capped", -5.8),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 67530.1,
    )
}
