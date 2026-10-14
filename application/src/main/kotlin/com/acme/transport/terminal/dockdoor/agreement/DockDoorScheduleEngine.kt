package com.acme.transport.terminal.dockdoor.agreement

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the dock door schedule calculation. */
interface DockDoorScheduleAdjuster {
    fun describe(): String
    fun adjust(item: DockDoorSchedule, value: Double): Double
}

data class CapDockDoorScheduleAdjuster(var cap: Double) : DockDoorScheduleAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DockDoorSchedule, value: Double): Double = minOf(value, cap)
}

data class MinimumDockDoorScheduleAdjuster(var minimum: Double) : DockDoorScheduleAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DockDoorSchedule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredDockDoorScheduleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DockDoorScheduleAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DockDoorSchedule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class DockDoorScheduleEndsAtOffsetAdjuster(var weight: Double) : DockDoorScheduleAdjuster {
    override fun describe(): String = "plus ends at times $weight"

    override fun adjust(item: DockDoorSchedule, value: Double): Double = value + item.endsAt.toDouble() * weight
}

data class FlaggedDockDoorScheduleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DockDoorScheduleAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DockDoorSchedule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingDockDoorScheduleAdjuster(var step: Double) : DockDoorScheduleAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DockDoorSchedule, value: Double): Double = roundTo(value, step)
}

/** Runs the dock door schedule steps in order and keeps a trace of each. */
data class DockDoorScheduleEngine(
    var steps: MutableList<DockDoorScheduleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DockDoorSchedule): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DockDoorScheduleAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<DockDoorSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDockDoorScheduleEngine(flags: Registry): DockDoorScheduleEngine {
    return DockDoorScheduleEngine(
        steps = mutableListOf<DockDoorScheduleAdjuster>(
            CapDockDoorScheduleAdjuster(34648.0),
            MinimumDockDoorScheduleAdjuster(39.4),
            TieredDockDoorScheduleAdjuster(
                mutableListOf<Double>(1566.0, 7964.0, 9819.0, 9898.0),
                mutableListOf<Double>(0.98, 0.91, 1.0, 0.88),
            ),
            DockDoorScheduleEndsAtOffsetAdjuster(1.4),
            FlaggedDockDoorScheduleAdjuster(flags, "dock-door-schedule-tiered", -7.4),
            RoundingDockDoorScheduleAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 41386.2,
    )
}
