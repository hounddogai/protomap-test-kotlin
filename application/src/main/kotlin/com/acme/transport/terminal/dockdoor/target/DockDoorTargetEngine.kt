package com.acme.transport.terminal.dockdoor.target

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the dock door target calculation. */
interface DockDoorTargetModifier {
    fun describe(): String
    fun adjust(item: DockDoorTarget, value: Double): Double
}

data class MinimumDockDoorTargetAdjuster(var minimum: Double) : DockDoorTargetModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DockDoorTarget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapDockDoorTargetAdjuster(var cap: Double) : DockDoorTargetModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DockDoorTarget, value: Double): Double = minOf(value, cap)
}

data class PercentDockDoorTargetAdjuster(var percent: Double) : DockDoorTargetModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DockDoorTarget, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the dock door target steps in order and keeps a trace of each. */
data class DockDoorTargetEngine(
    var steps: MutableList<DockDoorTargetModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DockDoorTarget): Double {
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

    fun evaluateAll(items: MutableList<DockDoorTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDockDoorTargetEngine(): DockDoorTargetEngine {
    return DockDoorTargetEngine(
        steps = mutableListOf<DockDoorTargetModifier>(
            MinimumDockDoorTargetAdjuster(45.7),
            CapDockDoorTargetAdjuster(29457.0),
            PercentDockDoorTargetAdjuster(2.2),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 92747.4,
    )
}
