package com.acme.transport.fleetops.utilization.entry

import com.acme.transport.kit.flags.Registry

/** One step of the global utilization entry calculation. */
interface GlobalUtilizationEntryAdjuster {
    fun describe(): String
    fun adjust(item: GlobalUtilizationEntry, value: Double): Double
}

data class GlobalUtilizationEntryPalletsOffsetAdjuster(var weight: Double) : GlobalUtilizationEntryAdjuster {
    override fun describe(): String = "plus pallets times $weight"

    override fun adjust(item: GlobalUtilizationEntry, value: Double): Double {
        return value + item.pallets.toDouble() * weight
    }
}

data class CapGlobalUtilizationEntryAdjuster(var cap: Double) : GlobalUtilizationEntryAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalUtilizationEntry, value: Double): Double = minOf(value, cap)
}

data class FlaggedGlobalUtilizationEntryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalUtilizationEntryAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalUtilizationEntry, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumGlobalUtilizationEntryAdjuster(var minimum: Double) : GlobalUtilizationEntryAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalUtilizationEntry, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the global utilization entry steps in order and keeps a trace of each. */
data class GlobalUtilizationEntryEngine(
    var steps: MutableList<GlobalUtilizationEntryAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: GlobalUtilizationEntry): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalUtilizationEntryAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalUtilizationEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultGlobalUtilizationEntryEngine(flags: Registry): GlobalUtilizationEntryEngine {
    return GlobalUtilizationEntryEngine(
        steps = mutableListOf<GlobalUtilizationEntryAdjuster>(
            GlobalUtilizationEntryPalletsOffsetAdjuster(1.34),
            CapGlobalUtilizationEntryAdjuster(19437.0),
            FlaggedGlobalUtilizationEntryAdjuster(flags, "global-utilization-entry-manual", -0.4),
            MinimumGlobalUtilizationEntryAdjuster(13.8),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 39189.5,
    )
}
