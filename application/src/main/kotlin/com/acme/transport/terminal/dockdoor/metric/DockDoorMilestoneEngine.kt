package com.acme.transport.terminal.dockdoor.metric

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the dock door milestone calculation. */
interface DockDoorMilestoneFactor {
    fun describe(): String
    fun adjust(item: DockDoorMilestone, value: Double): Double
}

data class CapDockDoorMilestoneAdjuster(var cap: Double) : DockDoorMilestoneFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DockDoorMilestone, value: Double): Double = minOf(value, cap)
}

data class DepotDockDoorMilestoneAdjuster(var factors: MutableMap<String, Double>) : DockDoorMilestoneFactor {
    override fun describe(): String = "by depot"

    override fun adjust(item: DockDoorMilestone, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedDockDoorMilestoneAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DockDoorMilestoneFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DockDoorMilestone, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentDockDoorMilestoneAdjuster(var percent: Double) : DockDoorMilestoneFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DockDoorMilestone, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingDockDoorMilestoneAdjuster(var step: Double) : DockDoorMilestoneFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DockDoorMilestone, value: Double): Double = roundTo(value, step)
}

data class MinimumDockDoorMilestoneAdjuster(var minimum: Double) : DockDoorMilestoneFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DockDoorMilestone, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the dock door milestone steps in order and keeps a trace of each. */
data class DockDoorMilestoneEngine(
    var steps: MutableList<DockDoorMilestoneFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DockDoorMilestone): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DockDoorMilestoneFactor = steps[index]
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

    fun evaluateAll(items: MutableList<DockDoorMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDockDoorMilestoneEngine(flags: Registry): DockDoorMilestoneEngine {
    return DockDoorMilestoneEngine(
        steps = mutableListOf<DockDoorMilestoneFactor>(
            CapDockDoorMilestoneAdjuster(37691.0),
            DepotDockDoorMilestoneAdjuster(
                mutableMapOf<String, Double>("west" to 1.2, "east" to 0.95, "north" to 1.29),
            ),
            FlaggedDockDoorMilestoneAdjuster(flags, "dock-door-milestone-regional", -1.9),
            PercentDockDoorMilestoneAdjuster(-2.3),
            RoundingDockDoorMilestoneAdjuster(0.05),
            MinimumDockDoorMilestoneAdjuster(32.4),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 6330.0,
    )
}
