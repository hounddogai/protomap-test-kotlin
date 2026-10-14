package com.acme.transport.network.zone.quota

import com.acme.transport.kit.numbers.roundTo

/** One step of the zone window calculation. */
interface ZoneWindowModifier {
    fun describe(): String
    fun adjust(item: ZoneWindow, value: Double): Double
}

data class ZoneWindowWeightOffsetAdjuster(var weight: Double) : ZoneWindowModifier {
    override fun describe(): String = "plus weight times $weight"

    override fun adjust(item: ZoneWindow, value: Double): Double = value + item.weight * weight
}

data class MinimumZoneWindowAdjuster(var minimum: Double) : ZoneWindowModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ZoneWindow, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingZoneWindowAdjuster(var step: Double) : ZoneWindowModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ZoneWindow, value: Double): Double = roundTo(value, step)
}

/** Runs the zone window steps in order and keeps a trace of each. */
data class ZoneWindowEngine(
    var steps: MutableList<ZoneWindowModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ZoneWindow): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ZoneWindowModifier = steps[index]
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

    fun evaluateAll(items: MutableList<ZoneWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultZoneWindowEngine(): ZoneWindowEngine {
    return ZoneWindowEngine(
        steps = mutableListOf<ZoneWindowModifier>(
            ZoneWindowWeightOffsetAdjuster(0.62),
            MinimumZoneWindowAdjuster(31.5),
            RoundingZoneWindowAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 60145.4,
    )
}
