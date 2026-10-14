package com.acme.transport.terminal.dockdoor.score

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the dock door score calculation. */
interface DockDoorScoreStep {
    fun describe(): String
    fun adjust(item: DockDoorScore, value: Double): Double
}

data class PercentDockDoorScoreAdjuster(var percent: Double) : DockDoorScoreStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DockDoorScore, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingDockDoorScoreAdjuster(var step: Double) : DockDoorScoreStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DockDoorScore, value: Double): Double = roundTo(value, step)
}

data class MinimumDockDoorScoreAdjuster(var minimum: Double) : DockDoorScoreStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DockDoorScore, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapDockDoorScoreAdjuster(var cap: Double) : DockDoorScoreStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DockDoorScore, value: Double): Double = minOf(value, cap)
}

/** Runs the dock door score steps in order and keeps a trace of each. */
data class DockDoorScoreEngine(
    var steps: MutableList<DockDoorScoreStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DockDoorScore): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DockDoorScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDockDoorScoreEngine(): DockDoorScoreEngine {
    return DockDoorScoreEngine(
        steps = mutableListOf<DockDoorScoreStep>(
            PercentDockDoorScoreAdjuster(-16.0),
            RoundingDockDoorScoreAdjuster(0.1),
            MinimumDockDoorScoreAdjuster(36.9),
            CapDockDoorScoreAdjuster(27194.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 29483.5,
    )
}
