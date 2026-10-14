package com.acme.transport.terminal.dockdoor.rule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the dock door rollup calculation. */
interface DockDoorRollupAdjuster {
    fun describe(): String
    fun adjust(item: DockDoorRollup, value: Double): Double
}

data class FlaggedDockDoorRollupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DockDoorRollupAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DockDoorRollup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingDockDoorRollupAdjuster(var step: Double) : DockDoorRollupAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DockDoorRollup, value: Double): Double = roundTo(value, step)
}

data class CapDockDoorRollupAdjuster(var cap: Double) : DockDoorRollupAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DockDoorRollup, value: Double): Double = minOf(value, cap)
}

data class MinimumDockDoorRollupAdjuster(var minimum: Double) : DockDoorRollupAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DockDoorRollup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the dock door rollup steps in order and keeps a trace of each. */
data class DockDoorRollupEngine(
    var steps: MutableList<DockDoorRollupAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DockDoorRollup): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DockDoorRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDockDoorRollupEngine(flags: Registry): DockDoorRollupEngine {
    return DockDoorRollupEngine(
        steps = mutableListOf<DockDoorRollupAdjuster>(
            FlaggedDockDoorRollupAdjuster(flags, "dock-door-rollup-adaptive", 2.1),
            RoundingDockDoorRollupAdjuster(1.0),
            CapDockDoorRollupAdjuster(41561.0),
            MinimumDockDoorRollupAdjuster(5.4),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 12443.6,
    )
}
