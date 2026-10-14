package com.acme.transport.network.zone.zonebundle

import com.acme.transport.kit.flags.Registry

/** One step of the regional zone schedule calculation. */
interface RegionalZoneScheduleAdjuster {
    fun describe(): String
    fun adjust(item: RegionalZoneSchedule, value: Double): Double
}

data class RegionalZoneScheduleAmountOffsetAdjuster(var weight: Double) : RegionalZoneScheduleAdjuster {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: RegionalZoneSchedule, value: Double): Double = value + item.amount * weight
}

data class TieredRegionalZoneScheduleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalZoneScheduleAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalZoneSchedule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapRegionalZoneScheduleAdjuster(var cap: Double) : RegionalZoneScheduleAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalZoneSchedule, value: Double): Double = minOf(value, cap)
}

data class FlaggedRegionalZoneScheduleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalZoneScheduleAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalZoneSchedule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumRegionalZoneScheduleAdjuster(var minimum: Double) : RegionalZoneScheduleAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalZoneSchedule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the regional zone schedule steps in order and keeps a trace of each. */
data class RegionalZoneScheduleEngine(
    var steps: MutableList<RegionalZoneScheduleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RegionalZoneSchedule): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalZoneScheduleAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalZoneSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRegionalZoneScheduleEngine(flags: Registry): RegionalZoneScheduleEngine {
    return RegionalZoneScheduleEngine(
        steps = mutableListOf<RegionalZoneScheduleAdjuster>(
            RegionalZoneScheduleAmountOffsetAdjuster(0.49),
            TieredRegionalZoneScheduleAdjuster(
                mutableListOf<Double>(5738.0, 7094.0, 8330.0),
                mutableListOf<Double>(0.85, 0.83, 1.1),
            ),
            CapRegionalZoneScheduleAdjuster(31086.0),
            FlaggedRegionalZoneScheduleAdjuster(flags, "regional-zone-schedule-regional", 7.6),
            MinimumRegionalZoneScheduleAdjuster(33.6),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 1102.1,
    )
}
