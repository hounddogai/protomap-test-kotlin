package com.acme.transport.terminal.dwell.notice

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the dwell milestone calculation. */
interface DwellMilestoneStep {
    fun describe(): String
    fun adjust(item: DwellMilestone, value: Double): Double
}

data class SourceDwellMilestoneAdjuster(var factors: MutableMap<String, Double>) : DwellMilestoneStep {
    override fun describe(): String = "by source"

    override fun adjust(item: DwellMilestone, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapDwellMilestoneAdjuster(var cap: Double) : DwellMilestoneStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DwellMilestone, value: Double): Double = minOf(value, cap)
}

data class DwellMilestoneOdometerOffsetAdjuster(var weight: Double) : DwellMilestoneStep {
    override fun describe(): String = "plus odometer times $weight"

    override fun adjust(item: DwellMilestone, value: Double): Double = value + item.odometer.toDouble() * weight
}

data class MinimumDwellMilestoneAdjuster(var minimum: Double) : DwellMilestoneStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DwellMilestone, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingDwellMilestoneAdjuster(var step: Double) : DwellMilestoneStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DwellMilestone, value: Double): Double = roundTo(value, step)
}

/** Runs the dwell milestone steps in order and keeps a trace of each. */
data class DwellMilestoneEngine(
    var steps: MutableList<DwellMilestoneStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DwellMilestone): Double {
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

    fun evaluateAll(items: MutableList<DwellMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDwellMilestoneEngine(): DwellMilestoneEngine {
    return DwellMilestoneEngine(
        steps = mutableListOf<DwellMilestoneStep>(
            SourceDwellMilestoneAdjuster(
                mutableMapOf<String, Double>("north" to 1.07, "retail" to 0.86, "central" to 1.14),
            ),
            CapDwellMilestoneAdjuster(4811.0),
            DwellMilestoneOdometerOffsetAdjuster(0.57),
            MinimumDwellMilestoneAdjuster(4.9),
            RoundingDwellMilestoneAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 56431.5,
    )
}
