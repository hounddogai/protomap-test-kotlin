package com.acme.transport.terminal.dockdoor.window

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the dock door window calculation. */
interface DockDoorWindowAdjuster {
    fun describe(): String
    fun adjust(item: DockDoorWindow, value: Double): Double
}

data class FlaggedDockDoorWindowAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DockDoorWindowAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DockDoorWindow, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingDockDoorWindowAdjuster(var step: Double) : DockDoorWindowAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DockDoorWindow, value: Double): Double = roundTo(value, step)
}

data class TieredDockDoorWindowAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DockDoorWindowAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DockDoorWindow, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumDockDoorWindowAdjuster(var minimum: Double) : DockDoorWindowAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DockDoorWindow, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentDockDoorWindowAdjuster(var percent: Double) : DockDoorWindowAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DockDoorWindow, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class DockDoorWindowFloorValueOffsetAdjuster(var weight: Double) : DockDoorWindowAdjuster {
    override fun describe(): String = "plus floor value times $weight"

    override fun adjust(item: DockDoorWindow, value: Double): Double = value + item.floorValue * weight
}

/** Runs the dock door window steps in order and keeps a trace of each. */
data class DockDoorWindowEngine(
    var steps: MutableList<DockDoorWindowAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DockDoorWindow): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DockDoorWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDockDoorWindowEngine(flags: Registry): DockDoorWindowEngine {
    return DockDoorWindowEngine(
        steps = mutableListOf<DockDoorWindowAdjuster>(
            FlaggedDockDoorWindowAdjuster(flags, "dock-door-window-lenient", 0.5),
            RoundingDockDoorWindowAdjuster(5.0),
            TieredDockDoorWindowAdjuster(
                mutableListOf<Double>(771.0, 1195.0),
                mutableListOf<Double>(0.82, 0.82),
            ),
            MinimumDockDoorWindowAdjuster(26.4),
            PercentDockDoorWindowAdjuster(-10.1),
            DockDoorWindowFloorValueOffsetAdjuster(0.32),
        ),
        trace = mutableListOf<String>(),
        floor = 3.1,
        ceiling = 6067.8,
    )
}
