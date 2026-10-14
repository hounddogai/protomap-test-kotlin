package com.acme.transport.terminal.crossdock.slot

import com.acme.transport.kit.flags.Registry

/** One step of the cross dock slot calculation. */
interface CrossDockSlotFactor {
    fun describe(): String
    fun adjust(item: CrossDockSlot, value: Double): Double
}

data class LaneCrossDockSlotAdjuster(var factors: MutableMap<String, Double>) : CrossDockSlotFactor {
    override fun describe(): String = "by lane"

    override fun adjust(item: CrossDockSlot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedCrossDockSlotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CrossDockSlotFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CrossDockSlot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumCrossDockSlotAdjuster(var minimum: Double) : CrossDockSlotFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CrossDockSlot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the cross dock slot steps in order and keeps a trace of each. */
data class CrossDockSlotEngine(
    var steps: MutableList<CrossDockSlotFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CrossDockSlot): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CrossDockSlotFactor = steps[index]
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

    fun evaluateAll(items: MutableList<CrossDockSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCrossDockSlotEngine(flags: Registry): CrossDockSlotEngine {
    return CrossDockSlotEngine(
        steps = mutableListOf<CrossDockSlotFactor>(
            LaneCrossDockSlotAdjuster(
                mutableMapOf<String, Double>("online" to 1.08, "central" to 1.33, "east" to 0.87),
            ),
            FlaggedCrossDockSlotAdjuster(flags, "cross-dock-slot-pending", 0.3),
            MinimumCrossDockSlotAdjuster(7.9),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 7945.1,
    )
}
