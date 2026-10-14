package com.acme.transport.terminal.dockdoor.ledger

import com.acme.transport.kit.flags.Registry

/** One step of the global dock door group calculation. */
interface GlobalDockDoorGroupModifier {
    fun describe(): String
    fun adjust(item: GlobalDockDoorGroup, value: Double): Double
}

data class TieredGlobalDockDoorGroupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalDockDoorGroupModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalDockDoorGroup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapGlobalDockDoorGroupAdjuster(var cap: Double) : GlobalDockDoorGroupModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalDockDoorGroup, value: Double): Double = minOf(value, cap)
}

data class FlaggedGlobalDockDoorGroupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalDockDoorGroupModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalDockDoorGroup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumGlobalDockDoorGroupAdjuster(var minimum: Double) : GlobalDockDoorGroupModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalDockDoorGroup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentGlobalDockDoorGroupAdjuster(var percent: Double) : GlobalDockDoorGroupModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalDockDoorGroup, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the global dock door group steps in order and keeps a trace of each. */
data class GlobalDockDoorGroupEngine(
    var steps: MutableList<GlobalDockDoorGroupModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: GlobalDockDoorGroup): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalDockDoorGroupModifier = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalDockDoorGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultGlobalDockDoorGroupEngine(flags: Registry): GlobalDockDoorGroupEngine {
    return GlobalDockDoorGroupEngine(
        steps = mutableListOf<GlobalDockDoorGroupModifier>(
            TieredGlobalDockDoorGroupAdjuster(
                mutableListOf<Double>(3010.0, 3322.0, 5559.0),
                mutableListOf<Double>(1.06, 0.98, 1.05),
            ),
            CapGlobalDockDoorGroupAdjuster(45749.0),
            FlaggedGlobalDockDoorGroupAdjuster(flags, "global-dock-door-group-fallback", -3.1),
            MinimumGlobalDockDoorGroupAdjuster(24.5),
            PercentGlobalDockDoorGroupAdjuster(-16.2),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 65530.1,
    )
}
