package com.acme.transport.terminal.dockdoor.window

import com.acme.transport.kit.numbers.roundTo

/** One step of the dock door baseline calculation. */
interface DockDoorBaselineAdjuster {
    fun describe(): String
    fun adjust(item: DockDoorBaseline, value: Double): Double
}

data class DockDoorBaselineSequenceOffsetAdjuster(var weight: Double) : DockDoorBaselineAdjuster {
    override fun describe(): String = "plus sequence times $weight"

    override fun adjust(item: DockDoorBaseline, value: Double): Double = value + item.sequence.toDouble() * weight
}

data class RoundingDockDoorBaselineAdjuster(var step: Double) : DockDoorBaselineAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DockDoorBaseline, value: Double): Double = roundTo(value, step)
}

data class SegmentDockDoorBaselineAdjuster(var factors: MutableMap<String, Double>) : DockDoorBaselineAdjuster {
    override fun describe(): String = "by segment"

    override fun adjust(item: DockDoorBaseline, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredDockDoorBaselineAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DockDoorBaselineAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DockDoorBaseline, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the dock door baseline steps in order and keeps a trace of each. */
data class DockDoorBaselineEngine(
    var steps: MutableList<DockDoorBaselineAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DockDoorBaseline): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DockDoorBaselineAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<DockDoorBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDockDoorBaselineEngine(): DockDoorBaselineEngine {
    return DockDoorBaselineEngine(
        steps = mutableListOf<DockDoorBaselineAdjuster>(
            DockDoorBaselineSequenceOffsetAdjuster(0.72),
            RoundingDockDoorBaselineAdjuster(0.1),
            SegmentDockDoorBaselineAdjuster(mutableMapOf<String, Double>("north" to 0.96)),
            TieredDockDoorBaselineAdjuster(
                mutableListOf<Double>(688.0, 7677.0, 8539.0, 9536.0),
                mutableListOf<Double>(1.16, 1.05, 0.88, 1.14),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 52219.0,
    )
}
