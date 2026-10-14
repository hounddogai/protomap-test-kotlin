package com.acme.transport.terminal.dockdoor.doorestimate

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the scheduled dock door assignment calculation. */
interface ScheduledDockDoorAssignmentAdjuster {
    fun describe(): String
    fun adjust(item: ScheduledDockDoorAssignment, value: Double): Double
}

data class MinimumScheduledDockDoorAssignmentAdjuster(
    var minimum: Double,
) : ScheduledDockDoorAssignmentAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledDockDoorAssignment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class ScheduledDockDoorAssignmentPalletsOffsetAdjuster(
    var weight: Double,
) : ScheduledDockDoorAssignmentAdjuster {
    override fun describe(): String = "plus pallets times $weight"

    override fun adjust(item: ScheduledDockDoorAssignment, value: Double): Double {
        return value + item.pallets.toDouble() * weight
    }
}

data class LabelScheduledDockDoorAssignmentAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledDockDoorAssignmentAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: ScheduledDockDoorAssignment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapScheduledDockDoorAssignmentAdjuster(var cap: Double) : ScheduledDockDoorAssignmentAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledDockDoorAssignment, value: Double): Double = minOf(value, cap)
}

data class RoundingScheduledDockDoorAssignmentAdjuster(var step: Double) : ScheduledDockDoorAssignmentAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledDockDoorAssignment, value: Double): Double = roundTo(value, step)
}

data class FlaggedScheduledDockDoorAssignmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledDockDoorAssignmentAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledDockDoorAssignment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the scheduled dock door assignment steps in order and keeps a trace of each. */
data class ScheduledDockDoorAssignmentEngine(
    var steps: MutableList<ScheduledDockDoorAssignmentAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ScheduledDockDoorAssignment): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ScheduledDockDoorAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultScheduledDockDoorAssignmentEngine(flags: Registry): ScheduledDockDoorAssignmentEngine {
    return ScheduledDockDoorAssignmentEngine(
        steps = mutableListOf<ScheduledDockDoorAssignmentAdjuster>(
            MinimumScheduledDockDoorAssignmentAdjuster(26.5),
            ScheduledDockDoorAssignmentPalletsOffsetAdjuster(0.12),
            LabelScheduledDockDoorAssignmentAdjuster(
                mutableMapOf<String, Double>("west" to 1.02, "online" to 0.88),
            ),
            CapScheduledDockDoorAssignmentAdjuster(22187.0),
            RoundingScheduledDockDoorAssignmentAdjuster(0.05),
            FlaggedScheduledDockDoorAssignmentAdjuster(flags, "scheduled-dock-door-assignment-regional", 7.2),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 62578.0,
    )
}
