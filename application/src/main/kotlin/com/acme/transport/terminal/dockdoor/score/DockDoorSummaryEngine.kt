package com.acme.transport.terminal.dockdoor.score

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the dock door summary calculation. */
interface DockDoorSummaryStep {
    fun describe(): String
    fun adjust(item: DockDoorSummary, value: Double): Double
}

data class MinimumDockDoorSummaryAdjuster(var minimum: Double) : DockDoorSummaryStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DockDoorSummary, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingDockDoorSummaryAdjuster(var step: Double) : DockDoorSummaryStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DockDoorSummary, value: Double): Double = roundTo(value, step)
}

data class TieredDockDoorSummaryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DockDoorSummaryStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DockDoorSummary, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapDockDoorSummaryAdjuster(var cap: Double) : DockDoorSummaryStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DockDoorSummary, value: Double): Double = minOf(value, cap)
}

data class FlaggedDockDoorSummaryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DockDoorSummaryStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DockDoorSummary, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the dock door summary steps in order and keeps a trace of each. */
data class DockDoorSummaryEngine(
    var steps: MutableList<DockDoorSummaryStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DockDoorSummary): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DockDoorSummaryStep = steps[index]
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

    fun evaluateAll(items: MutableList<DockDoorSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDockDoorSummaryEngine(flags: Registry): DockDoorSummaryEngine {
    return DockDoorSummaryEngine(
        steps = mutableListOf<DockDoorSummaryStep>(
            MinimumDockDoorSummaryAdjuster(49.6),
            RoundingDockDoorSummaryAdjuster(0.5),
            TieredDockDoorSummaryAdjuster(
                mutableListOf<Double>(6099.0, 8426.0, 8762.0, 9624.0),
                mutableListOf<Double>(0.85, 0.87, 1.07, 0.87),
            ),
            CapDockDoorSummaryAdjuster(16533.0),
            FlaggedDockDoorSummaryAdjuster(flags, "dock-door-summary-scheduled", -7.2),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 24413.9,
    )
}
