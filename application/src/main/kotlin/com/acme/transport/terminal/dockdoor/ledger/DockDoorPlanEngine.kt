package com.acme.transport.terminal.dockdoor.ledger

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the dock door plan calculation. */
interface DockDoorPlanStep {
    fun describe(): String
    fun adjust(item: DockDoorPlan, value: Double): Double
}

data class TieredDockDoorPlanAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DockDoorPlanStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DockDoorPlan, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingDockDoorPlanAdjuster(var step: Double) : DockDoorPlanStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DockDoorPlan, value: Double): Double = roundTo(value, step)
}

data class PercentDockDoorPlanAdjuster(var percent: Double) : DockDoorPlanStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DockDoorPlan, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedDockDoorPlanAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DockDoorPlanStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DockDoorPlan, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class DockDoorPlanSequenceOffsetAdjuster(var weight: Double) : DockDoorPlanStep {
    override fun describe(): String = "plus sequence times $weight"

    override fun adjust(item: DockDoorPlan, value: Double): Double = value + item.sequence.toDouble() * weight
}

data class MinimumDockDoorPlanAdjuster(var minimum: Double) : DockDoorPlanStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DockDoorPlan, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the dock door plan steps in order and keeps a trace of each. */
data class DockDoorPlanEngine(
    var steps: MutableList<DockDoorPlanStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DockDoorPlan): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DockDoorPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDockDoorPlanEngine(flags: Registry): DockDoorPlanEngine {
    return DockDoorPlanEngine(
        steps = mutableListOf<DockDoorPlanStep>(
            TieredDockDoorPlanAdjuster(
                mutableListOf<Double>(1161.0, 2948.0, 5425.0),
                mutableListOf<Double>(1.11, 1.04, 1.18),
            ),
            RoundingDockDoorPlanAdjuster(0.25),
            PercentDockDoorPlanAdjuster(-22.0),
            FlaggedDockDoorPlanAdjuster(flags, "dock-door-plan-rolling", 1.6),
            DockDoorPlanSequenceOffsetAdjuster(0.22),
            MinimumDockDoorPlanAdjuster(22.2),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 84186.0,
    )
}
