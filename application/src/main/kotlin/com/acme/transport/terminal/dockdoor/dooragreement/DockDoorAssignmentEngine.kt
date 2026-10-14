package com.acme.transport.terminal.dockdoor.dooragreement

import com.acme.transport.kit.numbers.roundTo

/** One step of the dock door assignment calculation. */
interface DockDoorAssignmentAdjuster {
    fun describe(): String
    fun adjust(item: DockDoorAssignment, value: Double): Double
}

data class CapDockDoorAssignmentAdjuster(var cap: Double) : DockDoorAssignmentAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DockDoorAssignment, value: Double): Double = minOf(value, cap)
}

data class RoundingDockDoorAssignmentAdjuster(var step: Double) : DockDoorAssignmentAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DockDoorAssignment, value: Double): Double = roundTo(value, step)
}

data class TieredDockDoorAssignmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DockDoorAssignmentAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DockDoorAssignment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class DockDoorAssignmentUpdatedAtOffsetAdjuster(var weight: Double) : DockDoorAssignmentAdjuster {
    override fun describe(): String = "plus updated at times $weight"

    override fun adjust(item: DockDoorAssignment, value: Double): Double = value + item.updatedAt.toDouble() * weight
}

data class PercentDockDoorAssignmentAdjuster(var percent: Double) : DockDoorAssignmentAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DockDoorAssignment, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the dock door assignment steps in order and keeps a trace of each. */
data class DockDoorAssignmentEngine(
    var steps: MutableList<DockDoorAssignmentAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DockDoorAssignment): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DockDoorAssignmentAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<DockDoorAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDockDoorAssignmentEngine(): DockDoorAssignmentEngine {
    return DockDoorAssignmentEngine(
        steps = mutableListOf<DockDoorAssignmentAdjuster>(
            CapDockDoorAssignmentAdjuster(18807.0),
            RoundingDockDoorAssignmentAdjuster(0.25),
            TieredDockDoorAssignmentAdjuster(
                mutableListOf<Double>(208.0, 2056.0, 4427.0, 6402.0),
                mutableListOf<Double>(1.19, 0.81, 0.81, 1.19),
            ),
            DockDoorAssignmentUpdatedAtOffsetAdjuster(1.8),
            PercentDockDoorAssignmentAdjuster(-0.6),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 9483.5,
    )
}
