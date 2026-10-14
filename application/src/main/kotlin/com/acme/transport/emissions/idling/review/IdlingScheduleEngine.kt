package com.acme.transport.emissions.idling.review

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the idling schedule calculation. */
interface IdlingScheduleModifier {
    fun describe(): String
    fun adjust(item: IdlingSchedule, value: Double): Double
}

data class MinimumIdlingScheduleAdjuster(var minimum: Double) : IdlingScheduleModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: IdlingSchedule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapIdlingScheduleAdjuster(var cap: Double) : IdlingScheduleModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: IdlingSchedule, value: Double): Double = minOf(value, cap)
}

data class IdlingScheduleAmountOffsetAdjuster(var weight: Double) : IdlingScheduleModifier {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: IdlingSchedule, value: Double): Double = value + item.amount * weight
}

/** Runs the idling schedule steps in order and keeps a trace of each. */
data class IdlingScheduleEngine(
    var steps: MutableList<IdlingScheduleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: IdlingSchedule): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<IdlingSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultIdlingScheduleEngine(): IdlingScheduleEngine {
    return IdlingScheduleEngine(
        steps = mutableListOf<IdlingScheduleModifier>(
            MinimumIdlingScheduleAdjuster(40.6),
            CapIdlingScheduleAdjuster(18983.0),
            IdlingScheduleAmountOffsetAdjuster(1.28),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 4947.2,
    )
}
