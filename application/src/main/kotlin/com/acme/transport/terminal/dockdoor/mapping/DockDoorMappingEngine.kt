package com.acme.transport.terminal.dockdoor.mapping

import com.acme.transport.kit.flags.Registry

/** One step of the dock door mapping calculation. */
interface DockDoorMappingAdjuster {
    fun describe(): String
    fun adjust(item: DockDoorMapping, value: Double): Double
}

data class FlaggedDockDoorMappingAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DockDoorMappingAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DockDoorMapping, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentDockDoorMappingAdjuster(var percent: Double) : DockDoorMappingAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DockDoorMapping, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredDockDoorMappingAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DockDoorMappingAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DockDoorMapping, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class DockDoorMappingEndsAtOffsetAdjuster(var weight: Double) : DockDoorMappingAdjuster {
    override fun describe(): String = "plus ends at times $weight"

    override fun adjust(item: DockDoorMapping, value: Double): Double = value + item.endsAt.toDouble() * weight
}

data class SegmentDockDoorMappingAdjuster(var factors: MutableMap<String, Double>) : DockDoorMappingAdjuster {
    override fun describe(): String = "by segment"

    override fun adjust(item: DockDoorMapping, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapDockDoorMappingAdjuster(var cap: Double) : DockDoorMappingAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DockDoorMapping, value: Double): Double = minOf(value, cap)
}

/** Runs the dock door mapping steps in order and keeps a trace of each. */
data class DockDoorMappingEngine(
    var steps: MutableList<DockDoorMappingAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DockDoorMapping): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DockDoorMappingAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<DockDoorMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDockDoorMappingEngine(flags: Registry): DockDoorMappingEngine {
    return DockDoorMappingEngine(
        steps = mutableListOf<DockDoorMappingAdjuster>(
            FlaggedDockDoorMappingAdjuster(flags, "dock-door-mapping-adaptive", -3.4),
            PercentDockDoorMappingAdjuster(-20.2),
            TieredDockDoorMappingAdjuster(
                mutableListOf<Double>(7841.0, 8038.0),
                mutableListOf<Double>(1.02, 1.14),
            ),
            DockDoorMappingEndsAtOffsetAdjuster(1.45),
            SegmentDockDoorMappingAdjuster(mutableMapOf<String, Double>("retail" to 1.28)),
            CapDockDoorMappingAdjuster(27001.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.0,
        ceiling = 13584.7,
    )
}
