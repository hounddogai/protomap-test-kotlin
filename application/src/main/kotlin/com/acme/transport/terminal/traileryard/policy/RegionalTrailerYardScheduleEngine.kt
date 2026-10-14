package com.acme.transport.terminal.traileryard.policy

import com.acme.transport.kit.flags.Registry

/** One step of the regional trailer yard schedule calculation. */
interface RegionalTrailerYardScheduleFactor {
    fun describe(): String
    fun adjust(item: RegionalTrailerYardSchedule, value: Double): Double
}

data class RegionalTrailerYardScheduleLimitOffsetAdjuster(
    var weight: Double,
) : RegionalTrailerYardScheduleFactor {
    override fun describe(): String = "plus limit times $weight"

    override fun adjust(item: RegionalTrailerYardSchedule, value: Double): Double {
        return value + item.limit.toDouble() * weight
    }
}

data class LabelRegionalTrailerYardScheduleAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalTrailerYardScheduleFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: RegionalTrailerYardSchedule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedRegionalTrailerYardScheduleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalTrailerYardScheduleFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalTrailerYardSchedule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentRegionalTrailerYardScheduleAdjuster(var percent: Double) : RegionalTrailerYardScheduleFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalTrailerYardSchedule, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the regional trailer yard schedule steps in order and keeps a trace of each. */
data class RegionalTrailerYardScheduleEngine(
    var steps: MutableList<RegionalTrailerYardScheduleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RegionalTrailerYardSchedule): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalTrailerYardScheduleFactor = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalTrailerYardSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRegionalTrailerYardScheduleEngine(flags: Registry): RegionalTrailerYardScheduleEngine {
    return RegionalTrailerYardScheduleEngine(
        steps = mutableListOf<RegionalTrailerYardScheduleFactor>(
            RegionalTrailerYardScheduleLimitOffsetAdjuster(1.91),
            LabelRegionalTrailerYardScheduleAdjuster(
                mutableMapOf<String, Double>("online" to 0.92, "east" to 1.35),
            ),
            FlaggedRegionalTrailerYardScheduleAdjuster(flags, "regional-trailer-yard-schedule-regional", 1.3),
            PercentRegionalTrailerYardScheduleAdjuster(-18.5),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 15249.9,
    )
}
