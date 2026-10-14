package com.acme.transport.emissions.idling.batch

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the idling entry calculation. */
interface IdlingEntryAdjuster {
    fun describe(): String
    fun adjust(item: IdlingEntry, value: Double): Double
}

data class IdlingEntryWindowDaysOffsetAdjuster(var weight: Double) : IdlingEntryAdjuster {
    override fun describe(): String = "plus window days times $weight"

    override fun adjust(item: IdlingEntry, value: Double): Double = value + item.windowDays.toDouble() * weight
}

data class MinimumIdlingEntryAdjuster(var minimum: Double) : IdlingEntryAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: IdlingEntry, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentIdlingEntryAdjuster(var percent: Double) : IdlingEntryAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: IdlingEntry, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the idling entry steps in order and keeps a trace of each. */
data class IdlingEntryEngine(
    var steps: MutableList<IdlingEntryAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: IdlingEntry): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<IdlingEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultIdlingEntryEngine(): IdlingEntryEngine {
    return IdlingEntryEngine(
        steps = mutableListOf<IdlingEntryAdjuster>(
            IdlingEntryWindowDaysOffsetAdjuster(1.68),
            MinimumIdlingEntryAdjuster(5.2),
            PercentIdlingEntryAdjuster(-18.2),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 96608.7,
    )
}
