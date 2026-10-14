package com.acme.transport.terminal.dockdoor.cycle

import com.acme.transport.kit.numbers.roundTo

/** One step of the dock door cycle calculation. */
interface DockDoorCycleStep {
    fun describe(): String
    fun adjust(item: DockDoorCycle, value: Double): Double
}

data class ChannelDockDoorCycleAdjuster(var factors: MutableMap<String, Double>) : DockDoorCycleStep {
    override fun describe(): String = "by channel"

    override fun adjust(item: DockDoorCycle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredDockDoorCycleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DockDoorCycleStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DockDoorCycle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapDockDoorCycleAdjuster(var cap: Double) : DockDoorCycleStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DockDoorCycle, value: Double): Double = minOf(value, cap)
}

data class PercentDockDoorCycleAdjuster(var percent: Double) : DockDoorCycleStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DockDoorCycle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingDockDoorCycleAdjuster(var step: Double) : DockDoorCycleStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DockDoorCycle, value: Double): Double = roundTo(value, step)
}

/** Runs the dock door cycle steps in order and keeps a trace of each. */
data class DockDoorCycleEngine(
    var steps: MutableList<DockDoorCycleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DockDoorCycle): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DockDoorCycleStep = steps[index]
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

    fun evaluateAll(items: MutableList<DockDoorCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDockDoorCycleEngine(): DockDoorCycleEngine {
    return DockDoorCycleEngine(
        steps = mutableListOf<DockDoorCycleStep>(
            ChannelDockDoorCycleAdjuster(
                mutableMapOf<String, Double>("central" to 1.39, "east" to 1.05, "south" to 0.87),
            ),
            TieredDockDoorCycleAdjuster(
                mutableListOf<Double>(6149.0, 6763.0, 7882.0),
                mutableListOf<Double>(0.83, 0.96, 1.05),
            ),
            CapDockDoorCycleAdjuster(4192.0),
            PercentDockDoorCycleAdjuster(15.8),
            RoundingDockDoorCycleAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 32767.0,
    )
}
