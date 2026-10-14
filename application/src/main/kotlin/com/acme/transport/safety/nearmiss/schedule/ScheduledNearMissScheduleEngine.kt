package com.acme.transport.safety.nearmiss.schedule

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the scheduled near miss schedule calculation. */
interface ScheduledNearMissScheduleStep {
    fun describe(): String
    fun adjust(item: ScheduledNearMissSchedule, value: Double): Double
}

data class TieredScheduledNearMissScheduleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledNearMissScheduleStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledNearMissSchedule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class SegmentScheduledNearMissScheduleAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledNearMissScheduleStep {
    override fun describe(): String = "by segment"

    override fun adjust(item: ScheduledNearMissSchedule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumScheduledNearMissScheduleAdjuster(var minimum: Double) : ScheduledNearMissScheduleStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledNearMissSchedule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentScheduledNearMissScheduleAdjuster(var percent: Double) : ScheduledNearMissScheduleStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledNearMissSchedule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapScheduledNearMissScheduleAdjuster(var cap: Double) : ScheduledNearMissScheduleStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledNearMissSchedule, value: Double): Double = minOf(value, cap)
}

/** Runs the scheduled near miss schedule steps in order and keeps a trace of each. */
data class ScheduledNearMissScheduleEngine(
    var steps: MutableList<ScheduledNearMissScheduleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ScheduledNearMissSchedule): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ScheduledNearMissSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultScheduledNearMissScheduleEngine(): ScheduledNearMissScheduleEngine {
    return ScheduledNearMissScheduleEngine(
        steps = mutableListOf<ScheduledNearMissScheduleStep>(
            TieredScheduledNearMissScheduleAdjuster(
                mutableListOf<Double>(7343.0, 8547.0),
                mutableListOf<Double>(0.84, 1.09),
            ),
            SegmentScheduledNearMissScheduleAdjuster(
                mutableMapOf<String, Double>("west" to 1.34, "retail" to 1.18),
            ),
            MinimumScheduledNearMissScheduleAdjuster(25.1),
            PercentScheduledNearMissScheduleAdjuster(15.9),
            CapScheduledNearMissScheduleAdjuster(14743.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 58191.3,
    )
}
