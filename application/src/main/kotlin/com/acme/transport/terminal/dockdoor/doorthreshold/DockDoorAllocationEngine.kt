package com.acme.transport.terminal.dockdoor.doorthreshold

import com.acme.transport.kit.numbers.roundTo

/** One step of the dock door allocation calculation. */
interface DockDoorAllocationModifier {
    fun describe(): String
    fun adjust(item: DockDoorAllocation, value: Double): Double
}

data class RoundingDockDoorAllocationAdjuster(var step: Double) : DockDoorAllocationModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DockDoorAllocation, value: Double): Double = roundTo(value, step)
}

data class TieredDockDoorAllocationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DockDoorAllocationModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DockDoorAllocation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumDockDoorAllocationAdjuster(var minimum: Double) : DockDoorAllocationModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DockDoorAllocation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TitleDockDoorAllocationAdjuster(
    var factors: MutableMap<String, Double>,
) : DockDoorAllocationModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: DockDoorAllocation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class DockDoorAllocationPriorityOffsetAdjuster(var weight: Double) : DockDoorAllocationModifier {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: DockDoorAllocation, value: Double): Double = value + item.priority.toDouble() * weight
}

/** Runs the dock door allocation steps in order and keeps a trace of each. */
data class DockDoorAllocationEngine(
    var steps: MutableList<DockDoorAllocationModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DockDoorAllocation): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DockDoorAllocationModifier = steps[index]
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

    fun evaluateAll(items: MutableList<DockDoorAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDockDoorAllocationEngine(): DockDoorAllocationEngine {
    return DockDoorAllocationEngine(
        steps = mutableListOf<DockDoorAllocationModifier>(
            RoundingDockDoorAllocationAdjuster(0.25),
            TieredDockDoorAllocationAdjuster(
                mutableListOf<Double>(4936.0, 8628.0),
                mutableListOf<Double>(1.0, 1.1),
            ),
            MinimumDockDoorAllocationAdjuster(41.0),
            TitleDockDoorAllocationAdjuster(mutableMapOf<String, Double>("east" to 1.07, "west" to 1.06)),
            DockDoorAllocationPriorityOffsetAdjuster(0.77),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 23952.9,
    )
}
