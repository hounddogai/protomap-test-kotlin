package com.acme.transport.customs.clearance.schedule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the fallback clearance schedule calculation. */
interface FallbackClearanceScheduleModifier {
    fun describe(): String
    fun adjust(item: FallbackClearanceSchedule, value: Double): Double
}

data class CapFallbackClearanceScheduleAdjuster(var cap: Double) : FallbackClearanceScheduleModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackClearanceSchedule, value: Double): Double = minOf(value, cap)
}

data class FallbackClearanceScheduleWeightOffsetAdjuster(
    var weight: Double,
) : FallbackClearanceScheduleModifier {
    override fun describe(): String = "plus weight times $weight"

    override fun adjust(item: FallbackClearanceSchedule, value: Double): Double = value + item.weight * weight
}

data class RegionFallbackClearanceScheduleAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackClearanceScheduleModifier {
    override fun describe(): String = "by region"

    override fun adjust(item: FallbackClearanceSchedule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedFallbackClearanceScheduleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackClearanceScheduleModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackClearanceSchedule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumFallbackClearanceScheduleAdjuster(var minimum: Double) : FallbackClearanceScheduleModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackClearanceSchedule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingFallbackClearanceScheduleAdjuster(var step: Double) : FallbackClearanceScheduleModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackClearanceSchedule, value: Double): Double = roundTo(value, step)
}

/** Runs the fallback clearance schedule steps in order and keeps a trace of each. */
data class FallbackClearanceScheduleEngine(
    var steps: MutableList<FallbackClearanceScheduleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: FallbackClearanceSchedule): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackClearanceScheduleModifier = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackClearanceSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultFallbackClearanceScheduleEngine(flags: Registry): FallbackClearanceScheduleEngine {
    return FallbackClearanceScheduleEngine(
        steps = mutableListOf<FallbackClearanceScheduleModifier>(
            CapFallbackClearanceScheduleAdjuster(32815.0),
            FallbackClearanceScheduleWeightOffsetAdjuster(0.38),
            RegionFallbackClearanceScheduleAdjuster(
                mutableMapOf<String, Double>("south" to 1.35, "central" to 0.99),
            ),
            FlaggedFallbackClearanceScheduleAdjuster(flags, "fallback-clearance-schedule-capped", 0.7),
            MinimumFallbackClearanceScheduleAdjuster(17.0),
            RoundingFallbackClearanceScheduleAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 95055.2,
    )
}
