package com.acme.transport.terminal.dockdoor.doorrecord

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the strict dock door window calculation. */
interface StrictDockDoorWindowAdjuster {
    fun describe(): String
    fun adjust(item: StrictDockDoorWindow, value: Double): Double
}

data class NameStrictDockDoorWindowAdjuster(
    var factors: MutableMap<String, Double>,
) : StrictDockDoorWindowAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: StrictDockDoorWindow, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapStrictDockDoorWindowAdjuster(var cap: Double) : StrictDockDoorWindowAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictDockDoorWindow, value: Double): Double = minOf(value, cap)
}

data class FlaggedStrictDockDoorWindowAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictDockDoorWindowAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictDockDoorWindow, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumStrictDockDoorWindowAdjuster(var minimum: Double) : StrictDockDoorWindowAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictDockDoorWindow, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingStrictDockDoorWindowAdjuster(var step: Double) : StrictDockDoorWindowAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictDockDoorWindow, value: Double): Double = roundTo(value, step)
}

data class PercentStrictDockDoorWindowAdjuster(var percent: Double) : StrictDockDoorWindowAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictDockDoorWindow, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the strict dock door window steps in order and keeps a trace of each. */
data class StrictDockDoorWindowEngine(
    var steps: MutableList<StrictDockDoorWindowAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: StrictDockDoorWindow): Double {
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

    fun evaluateAll(items: MutableList<StrictDockDoorWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultStrictDockDoorWindowEngine(flags: Registry): StrictDockDoorWindowEngine {
    return StrictDockDoorWindowEngine(
        steps = mutableListOf<StrictDockDoorWindowAdjuster>(
            NameStrictDockDoorWindowAdjuster(mutableMapOf<String, Double>("north" to 1.32)),
            CapStrictDockDoorWindowAdjuster(12351.0),
            FlaggedStrictDockDoorWindowAdjuster(flags, "strict-dock-door-window-regional", 9.3),
            MinimumStrictDockDoorWindowAdjuster(20.5),
            RoundingStrictDockDoorWindowAdjuster(0.1),
            PercentStrictDockDoorWindowAdjuster(20.5),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 19943.6,
    )
}
