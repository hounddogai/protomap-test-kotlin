package com.acme.transport.customs.clearance.primaryclearanceoverride

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the strict clearance schedule calculation. */
interface StrictClearanceScheduleFactor {
    fun describe(): String
    fun adjust(item: StrictClearanceSchedule, value: Double): Double
}

data class StrictClearanceScheduleLoadKgOffsetAdjuster(var weight: Double) : StrictClearanceScheduleFactor {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: StrictClearanceSchedule, value: Double): Double = value + item.loadKg * weight
}

data class FlaggedStrictClearanceScheduleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictClearanceScheduleFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictClearanceSchedule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingStrictClearanceScheduleAdjuster(var step: Double) : StrictClearanceScheduleFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictClearanceSchedule, value: Double): Double = roundTo(value, step)
}

data class LabelStrictClearanceScheduleAdjuster(
    var factors: MutableMap<String, Double>,
) : StrictClearanceScheduleFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: StrictClearanceSchedule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredStrictClearanceScheduleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictClearanceScheduleFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictClearanceSchedule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the strict clearance schedule steps in order and keeps a trace of each. */
data class StrictClearanceScheduleEngine(
    var steps: MutableList<StrictClearanceScheduleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: StrictClearanceSchedule): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictClearanceScheduleFactor = steps[index]
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

    fun evaluateAll(items: MutableList<StrictClearanceSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultStrictClearanceScheduleEngine(flags: Registry): StrictClearanceScheduleEngine {
    return StrictClearanceScheduleEngine(
        steps = mutableListOf<StrictClearanceScheduleFactor>(
            StrictClearanceScheduleLoadKgOffsetAdjuster(0.67),
            FlaggedStrictClearanceScheduleAdjuster(flags, "strict-clearance-schedule-primary", 4.9),
            RoundingStrictClearanceScheduleAdjuster(0.25),
            LabelStrictClearanceScheduleAdjuster(
                mutableMapOf<String, Double>("central" to 0.99, "online" to 1.06),
            ),
            TieredStrictClearanceScheduleAdjuster(
                mutableListOf<Double>(329.0, 1263.0, 2469.0, 8583.0),
                mutableListOf<Double>(1.13, 0.94, 0.81, 1.02),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 4617.4,
    )
}
