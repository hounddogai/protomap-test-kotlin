package com.acme.transport.terminal.dockdoor.doorsnapshot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the capped dock door snapshot calculation. */
interface CappedDockDoorSnapshotStep {
    fun describe(): String
    fun adjust(item: CappedDockDoorSnapshot, value: Double): Double
}

data class CapCappedDockDoorSnapshotAdjuster(var cap: Double) : CappedDockDoorSnapshotStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedDockDoorSnapshot, value: Double): Double = minOf(value, cap)
}

data class RoundingCappedDockDoorSnapshotAdjuster(var step: Double) : CappedDockDoorSnapshotStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedDockDoorSnapshot, value: Double): Double = roundTo(value, step)
}

data class MinimumCappedDockDoorSnapshotAdjuster(var minimum: Double) : CappedDockDoorSnapshotStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedDockDoorSnapshot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedCappedDockDoorSnapshotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedDockDoorSnapshotStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedDockDoorSnapshot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the capped dock door snapshot steps in order and keeps a trace of each. */
data class CappedDockDoorSnapshotEngine(
    var steps: MutableList<CappedDockDoorSnapshotStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CappedDockDoorSnapshot): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedDockDoorSnapshotStep = steps[index]
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

    fun evaluateAll(items: MutableList<CappedDockDoorSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCappedDockDoorSnapshotEngine(flags: Registry): CappedDockDoorSnapshotEngine {
    return CappedDockDoorSnapshotEngine(
        steps = mutableListOf<CappedDockDoorSnapshotStep>(
            CapCappedDockDoorSnapshotAdjuster(9687.0),
            RoundingCappedDockDoorSnapshotAdjuster(0.25),
            MinimumCappedDockDoorSnapshotAdjuster(8.4),
            FlaggedCappedDockDoorSnapshotAdjuster(flags, "capped-dock-door-snapshot-seasonal", 7.6),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 46209.5,
    )
}
