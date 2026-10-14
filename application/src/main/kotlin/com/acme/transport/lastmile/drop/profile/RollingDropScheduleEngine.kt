package com.acme.transport.lastmile.drop.profile

import com.acme.transport.kit.numbers.roundTo

/** One step of the rolling drop schedule calculation. */
interface RollingDropScheduleModifier {
    fun describe(): String
    fun adjust(item: RollingDropSchedule, value: Double): Double
}

data class CapRollingDropScheduleAdjuster(var cap: Double) : RollingDropScheduleModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingDropSchedule, value: Double): Double = minOf(value, cap)
}

data class RollingDropScheduleWeightOffsetAdjuster(var weight: Double) : RollingDropScheduleModifier {
    override fun describe(): String = "plus weight times $weight"

    override fun adjust(item: RollingDropSchedule, value: Double): Double = value + item.weight * weight
}

data class RoundingRollingDropScheduleAdjuster(var step: Double) : RollingDropScheduleModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingDropSchedule, value: Double): Double = roundTo(value, step)
}

/** Runs the rolling drop schedule steps in order and keeps a trace of each. */
data class RollingDropScheduleEngine(
    var steps: MutableList<RollingDropScheduleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RollingDropSchedule): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingDropScheduleModifier = steps[index]
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

    fun evaluateAll(items: MutableList<RollingDropSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRollingDropScheduleEngine(): RollingDropScheduleEngine {
    return RollingDropScheduleEngine(
        steps = mutableListOf<RollingDropScheduleModifier>(
            CapRollingDropScheduleAdjuster(4315.0),
            RollingDropScheduleWeightOffsetAdjuster(1.22),
            RoundingRollingDropScheduleAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 78807.8,
    )
}
