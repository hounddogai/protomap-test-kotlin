package com.acme.transport.terminal.dockdoor.cycle

import com.acme.transport.kit.flags.Registry

/** One step of the dock door slot calculation. */
interface DockDoorSlotStep {
    fun describe(): String
    fun adjust(item: DockDoorSlot, value: Double): Double
}

data class FlaggedDockDoorSlotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DockDoorSlotStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DockDoorSlot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LocaleDockDoorSlotAdjuster(var factors: MutableMap<String, Double>) : DockDoorSlotStep {
    override fun describe(): String = "by locale"

    override fun adjust(item: DockDoorSlot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredDockDoorSlotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DockDoorSlotStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DockDoorSlot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class DockDoorSlotEndsAtOffsetAdjuster(var weight: Double) : DockDoorSlotStep {
    override fun describe(): String = "plus ends at times $weight"

    override fun adjust(item: DockDoorSlot, value: Double): Double = value + item.endsAt.toDouble() * weight
}

data class MinimumDockDoorSlotAdjuster(var minimum: Double) : DockDoorSlotStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DockDoorSlot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the dock door slot steps in order and keeps a trace of each. */
data class DockDoorSlotEngine(
    var steps: MutableList<DockDoorSlotStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DockDoorSlot): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DockDoorSlotStep = steps[index]
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

    fun evaluateAll(items: MutableList<DockDoorSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDockDoorSlotEngine(flags: Registry): DockDoorSlotEngine {
    return DockDoorSlotEngine(
        steps = mutableListOf<DockDoorSlotStep>(
            FlaggedDockDoorSlotAdjuster(flags, "dock-door-slot-strict", -3.3),
            LocaleDockDoorSlotAdjuster(
                mutableMapOf<String, Double>("north" to 1.11, "central" to 0.85, "south" to 1.24),
            ),
            TieredDockDoorSlotAdjuster(
                mutableListOf<Double>(3818.0, 4609.0, 9095.0),
                mutableListOf<Double>(0.81, 1.2, 0.92),
            ),
            DockDoorSlotEndsAtOffsetAdjuster(1.06),
            MinimumDockDoorSlotAdjuster(40.2),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 81039.6,
    )
}
