package com.acme.transport.terminal.dockdoor.snapshot

import com.acme.transport.kit.flags.Registry

/** One step of the scheduled dock door snapshot calculation. */
interface ScheduledDockDoorSnapshotFactor {
    fun describe(): String
    fun adjust(item: ScheduledDockDoorSnapshot, value: Double): Double
}

data class FlaggedScheduledDockDoorSnapshotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledDockDoorSnapshotFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledDockDoorSnapshot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapScheduledDockDoorSnapshotAdjuster(var cap: Double) : ScheduledDockDoorSnapshotFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledDockDoorSnapshot, value: Double): Double = minOf(value, cap)
}

data class TieredScheduledDockDoorSnapshotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledDockDoorSnapshotFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledDockDoorSnapshot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumScheduledDockDoorSnapshotAdjuster(var minimum: Double) : ScheduledDockDoorSnapshotFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledDockDoorSnapshot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class ScheduledDockDoorSnapshotExpiresAtOffsetAdjuster(
    var weight: Double,
) : ScheduledDockDoorSnapshotFactor {
    override fun describe(): String = "plus expires at times $weight"

    override fun adjust(item: ScheduledDockDoorSnapshot, value: Double): Double {
        return value + item.expiresAt.toDouble() * weight
    }
}

/** Runs the scheduled dock door snapshot steps in order and keeps a trace of each. */
data class ScheduledDockDoorSnapshotEngine(
    var steps: MutableList<ScheduledDockDoorSnapshotFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ScheduledDockDoorSnapshot): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledDockDoorSnapshotFactor = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledDockDoorSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultScheduledDockDoorSnapshotEngine(flags: Registry): ScheduledDockDoorSnapshotEngine {
    return ScheduledDockDoorSnapshotEngine(
        steps = mutableListOf<ScheduledDockDoorSnapshotFactor>(
            FlaggedScheduledDockDoorSnapshotAdjuster(flags, "scheduled-dock-door-snapshot-seasonal", -1.6),
            CapScheduledDockDoorSnapshotAdjuster(43822.0),
            TieredScheduledDockDoorSnapshotAdjuster(
                mutableListOf<Double>(1689.0, 3287.0),
                mutableListOf<Double>(0.8, 0.96),
            ),
            MinimumScheduledDockDoorSnapshotAdjuster(30.4),
            ScheduledDockDoorSnapshotExpiresAtOffsetAdjuster(0.88),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 12512.5,
    )
}
