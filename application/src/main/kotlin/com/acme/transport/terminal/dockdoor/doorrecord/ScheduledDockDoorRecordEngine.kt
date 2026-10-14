package com.acme.transport.terminal.dockdoor.doorrecord

import com.acme.transport.kit.flags.Registry

/** One step of the scheduled dock door record calculation. */
interface ScheduledDockDoorRecordAdjuster {
    fun describe(): String
    fun adjust(item: ScheduledDockDoorRecord, value: Double): Double
}

data class FlaggedScheduledDockDoorRecordAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledDockDoorRecordAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledDockDoorRecord, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumScheduledDockDoorRecordAdjuster(var minimum: Double) : ScheduledDockDoorRecordAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledDockDoorRecord, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapScheduledDockDoorRecordAdjuster(var cap: Double) : ScheduledDockDoorRecordAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledDockDoorRecord, value: Double): Double = minOf(value, cap)
}

data class NameScheduledDockDoorRecordAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledDockDoorRecordAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: ScheduledDockDoorRecord, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the scheduled dock door record steps in order and keeps a trace of each. */
data class ScheduledDockDoorRecordEngine(
    var steps: MutableList<ScheduledDockDoorRecordAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ScheduledDockDoorRecord): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledDockDoorRecordAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledDockDoorRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultScheduledDockDoorRecordEngine(flags: Registry): ScheduledDockDoorRecordEngine {
    return ScheduledDockDoorRecordEngine(
        steps = mutableListOf<ScheduledDockDoorRecordAdjuster>(
            FlaggedScheduledDockDoorRecordAdjuster(flags, "scheduled-dock-door-record-batched", -8.2),
            MinimumScheduledDockDoorRecordAdjuster(24.9),
            CapScheduledDockDoorRecordAdjuster(9534.0),
            NameScheduledDockDoorRecordAdjuster(mutableMapOf<String, Double>("central" to 0.92)),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 65226.4,
    )
}
