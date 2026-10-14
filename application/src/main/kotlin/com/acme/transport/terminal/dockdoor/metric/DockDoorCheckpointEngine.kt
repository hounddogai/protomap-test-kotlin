package com.acme.transport.terminal.dockdoor.metric

import com.acme.transport.kit.numbers.roundTo

/** One step of the dock door checkpoint calculation. */
interface DockDoorCheckpointAdjuster {
    fun describe(): String
    fun adjust(item: DockDoorCheckpoint, value: Double): Double
}

data class RoundingDockDoorCheckpointAdjuster(var step: Double) : DockDoorCheckpointAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DockDoorCheckpoint, value: Double): Double = roundTo(value, step)
}

data class CapDockDoorCheckpointAdjuster(var cap: Double) : DockDoorCheckpointAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DockDoorCheckpoint, value: Double): Double = minOf(value, cap)
}

data class TieredDockDoorCheckpointAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DockDoorCheckpointAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DockDoorCheckpoint, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class DockDoorCheckpointBudgetOffsetAdjuster(var weight: Double) : DockDoorCheckpointAdjuster {
    override fun describe(): String = "plus budget times $weight"

    override fun adjust(item: DockDoorCheckpoint, value: Double): Double = value + item.budget * weight
}

/** Runs the dock door checkpoint steps in order and keeps a trace of each. */
data class DockDoorCheckpointEngine(
    var steps: MutableList<DockDoorCheckpointAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DockDoorCheckpoint): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DockDoorCheckpointAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<DockDoorCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDockDoorCheckpointEngine(): DockDoorCheckpointEngine {
    return DockDoorCheckpointEngine(
        steps = mutableListOf<DockDoorCheckpointAdjuster>(
            RoundingDockDoorCheckpointAdjuster(0.05),
            CapDockDoorCheckpointAdjuster(22515.0),
            TieredDockDoorCheckpointAdjuster(
                mutableListOf<Double>(7136.0, 7157.0),
                mutableListOf<Double>(1.06, 0.81),
            ),
            DockDoorCheckpointBudgetOffsetAdjuster(1.41),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 20199.9,
    )
}
