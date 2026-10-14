package com.acme.transport.terminal.dockdoor.exception

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the dock door entry calculation. */
interface DockDoorEntryStep {
    fun describe(): String
    fun adjust(item: DockDoorEntry, value: Double): Double
}

data class TieredDockDoorEntryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DockDoorEntryStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DockDoorEntry, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentDockDoorEntryAdjuster(var percent: Double) : DockDoorEntryStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DockDoorEntry, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedDockDoorEntryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DockDoorEntryStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DockDoorEntry, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class DockDoorEntryRetriesOffsetAdjuster(var weight: Double) : DockDoorEntryStep {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: DockDoorEntry, value: Double): Double = value + item.retries.toDouble() * weight
}

data class CapDockDoorEntryAdjuster(var cap: Double) : DockDoorEntryStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DockDoorEntry, value: Double): Double = minOf(value, cap)
}

/** Runs the dock door entry steps in order and keeps a trace of each. */
data class DockDoorEntryEngine(
    var steps: MutableList<DockDoorEntryStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DockDoorEntry): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DockDoorEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDockDoorEntryEngine(flags: Registry): DockDoorEntryEngine {
    return DockDoorEntryEngine(
        steps = mutableListOf<DockDoorEntryStep>(
            TieredDockDoorEntryAdjuster(
                mutableListOf<Double>(6893.0, 9714.0),
                mutableListOf<Double>(0.88, 1.06),
            ),
            PercentDockDoorEntryAdjuster(-5.4),
            FlaggedDockDoorEntryAdjuster(flags, "dock-door-entry-primary", -8.0),
            DockDoorEntryRetriesOffsetAdjuster(1.73),
            CapDockDoorEntryAdjuster(17647.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 67772.0,
    )
}
