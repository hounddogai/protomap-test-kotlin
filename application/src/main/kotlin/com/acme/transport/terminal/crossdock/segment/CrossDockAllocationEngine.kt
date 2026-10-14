package com.acme.transport.terminal.crossdock.segment

import com.acme.transport.kit.flags.Registry

/** One step of the cross dock allocation calculation. */
interface CrossDockAllocationModifier {
    fun describe(): String
    fun adjust(item: CrossDockAllocation, value: Double): Double
}

data class MinimumCrossDockAllocationAdjuster(var minimum: Double) : CrossDockAllocationModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CrossDockAllocation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedCrossDockAllocationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CrossDockAllocationModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CrossDockAllocation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentCrossDockAllocationAdjuster(var percent: Double) : CrossDockAllocationModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CrossDockAllocation, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the cross dock allocation steps in order and keeps a trace of each. */
data class CrossDockAllocationEngine(
    var steps: MutableList<CrossDockAllocationModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CrossDockAllocation): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CrossDockAllocationModifier = steps[index]
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

    fun evaluateAll(items: MutableList<CrossDockAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCrossDockAllocationEngine(flags: Registry): CrossDockAllocationEngine {
    return CrossDockAllocationEngine(
        steps = mutableListOf<CrossDockAllocationModifier>(
            MinimumCrossDockAllocationAdjuster(17.1),
            FlaggedCrossDockAllocationAdjuster(flags, "cross-dock-allocation-weighted", 6.7),
            PercentCrossDockAllocationAdjuster(1.3),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 57591.1,
    )
}
