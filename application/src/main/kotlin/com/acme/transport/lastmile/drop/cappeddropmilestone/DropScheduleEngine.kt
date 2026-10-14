package com.acme.transport.lastmile.drop.cappeddropmilestone

import com.acme.transport.kit.numbers.roundTo

/** One step of the drop schedule calculation. */
interface DropScheduleStep {
    fun describe(): String
    fun adjust(item: DropSchedule, value: Double): Double
}

data class CapDropScheduleAdjuster(var cap: Double) : DropScheduleStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DropSchedule, value: Double): Double = minOf(value, cap)
}

data class MinimumDropScheduleAdjuster(var minimum: Double) : DropScheduleStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DropSchedule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class DropScheduleRetriesOffsetAdjuster(var weight: Double) : DropScheduleStep {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: DropSchedule, value: Double): Double = value + item.retries.toDouble() * weight
}

data class RoundingDropScheduleAdjuster(var step: Double) : DropScheduleStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DropSchedule, value: Double): Double = roundTo(value, step)
}

/** Runs the drop schedule steps in order and keeps a trace of each. */
data class DropScheduleEngine(
    var steps: MutableList<DropScheduleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DropSchedule): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DropScheduleStep = steps[index]
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

    fun evaluateAll(items: MutableList<DropSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDropScheduleEngine(): DropScheduleEngine {
    return DropScheduleEngine(
        steps = mutableListOf<DropScheduleStep>(
            CapDropScheduleAdjuster(22580.0),
            MinimumDropScheduleAdjuster(14.4),
            DropScheduleRetriesOffsetAdjuster(0.23),
            RoundingDropScheduleAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 9847.4,
    )
}
