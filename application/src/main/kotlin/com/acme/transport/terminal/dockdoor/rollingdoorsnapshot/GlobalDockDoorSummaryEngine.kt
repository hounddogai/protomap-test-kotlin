package com.acme.transport.terminal.dockdoor.rollingdoorsnapshot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the global dock door summary calculation. */
interface GlobalDockDoorSummaryStep {
    fun describe(): String
    fun adjust(item: GlobalDockDoorSummary, value: Double): Double
}

data class FlaggedGlobalDockDoorSummaryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalDockDoorSummaryStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalDockDoorSummary, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class GlobalDockDoorSummaryWindowDaysOffsetAdjuster(var weight: Double) : GlobalDockDoorSummaryStep {
    override fun describe(): String = "plus window days times $weight"

    override fun adjust(item: GlobalDockDoorSummary, value: Double): Double {
        return value + item.windowDays.toDouble() * weight
    }
}

data class RoundingGlobalDockDoorSummaryAdjuster(var step: Double) : GlobalDockDoorSummaryStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalDockDoorSummary, value: Double): Double = roundTo(value, step)
}

data class MinimumGlobalDockDoorSummaryAdjuster(var minimum: Double) : GlobalDockDoorSummaryStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalDockDoorSummary, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredGlobalDockDoorSummaryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalDockDoorSummaryStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalDockDoorSummary, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapGlobalDockDoorSummaryAdjuster(var cap: Double) : GlobalDockDoorSummaryStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalDockDoorSummary, value: Double): Double = minOf(value, cap)
}

/** Runs the global dock door summary steps in order and keeps a trace of each. */
data class GlobalDockDoorSummaryEngine(
    var steps: MutableList<GlobalDockDoorSummaryStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: GlobalDockDoorSummary): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalDockDoorSummaryStep = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalDockDoorSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultGlobalDockDoorSummaryEngine(flags: Registry): GlobalDockDoorSummaryEngine {
    return GlobalDockDoorSummaryEngine(
        steps = mutableListOf<GlobalDockDoorSummaryStep>(
            FlaggedGlobalDockDoorSummaryAdjuster(flags, "global-dock-door-summary-strict", 5.0),
            GlobalDockDoorSummaryWindowDaysOffsetAdjuster(0.51),
            RoundingGlobalDockDoorSummaryAdjuster(0.5),
            MinimumGlobalDockDoorSummaryAdjuster(11.7),
            TieredGlobalDockDoorSummaryAdjuster(
                mutableListOf<Double>(151.0, 7923.0),
                mutableListOf<Double>(1.07, 0.95),
            ),
            CapGlobalDockDoorSummaryAdjuster(44003.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 51876.6,
    )
}
