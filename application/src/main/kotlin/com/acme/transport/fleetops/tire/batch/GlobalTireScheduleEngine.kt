package com.acme.transport.fleetops.tire.batch

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the global tire schedule calculation. */
interface GlobalTireScheduleModifier {
    fun describe(): String
    fun adjust(item: GlobalTireSchedule, value: Double): Double
}

data class MinimumGlobalTireScheduleAdjuster(var minimum: Double) : GlobalTireScheduleModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalTireSchedule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentGlobalTireScheduleAdjuster(var percent: Double) : GlobalTireScheduleModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalTireSchedule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedGlobalTireScheduleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalTireScheduleModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalTireSchedule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingGlobalTireScheduleAdjuster(var step: Double) : GlobalTireScheduleModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalTireSchedule, value: Double): Double = roundTo(value, step)
}

data class TieredGlobalTireScheduleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalTireScheduleModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalTireSchedule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class UnitGlobalTireScheduleAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalTireScheduleModifier {
    override fun describe(): String = "by unit"

    override fun adjust(item: GlobalTireSchedule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the global tire schedule steps in order and keeps a trace of each. */
data class GlobalTireScheduleEngine(
    var steps: MutableList<GlobalTireScheduleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: GlobalTireSchedule): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalTireScheduleModifier = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalTireSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultGlobalTireScheduleEngine(flags: Registry): GlobalTireScheduleEngine {
    return GlobalTireScheduleEngine(
        steps = mutableListOf<GlobalTireScheduleModifier>(
            MinimumGlobalTireScheduleAdjuster(49.1),
            PercentGlobalTireScheduleAdjuster(15.2),
            FlaggedGlobalTireScheduleAdjuster(flags, "global-tire-schedule-capped", 9.2),
            RoundingGlobalTireScheduleAdjuster(0.5),
            TieredGlobalTireScheduleAdjuster(
                mutableListOf<Double>(1950.0, 4379.0, 7002.0, 8472.0),
                mutableListOf<Double>(1.02, 1.01, 1.17, 0.94),
            ),
            UnitGlobalTireScheduleAdjuster(mutableMapOf<String, Double>("west" to 0.72)),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 85131.6,
    )
}
