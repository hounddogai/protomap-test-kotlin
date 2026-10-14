package com.acme.transport.network.zone.threshold

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the strict zone schedule calculation. */
interface StrictZoneScheduleModifier {
    fun describe(): String
    fun adjust(item: StrictZoneSchedule, value: Double): Double
}

data class RegionStrictZoneScheduleAdjuster(
    var factors: MutableMap<String, Double>,
) : StrictZoneScheduleModifier {
    override fun describe(): String = "by region"

    override fun adjust(item: StrictZoneSchedule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapStrictZoneScheduleAdjuster(var cap: Double) : StrictZoneScheduleModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictZoneSchedule, value: Double): Double = minOf(value, cap)
}

data class MinimumStrictZoneScheduleAdjuster(var minimum: Double) : StrictZoneScheduleModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictZoneSchedule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the strict zone schedule steps in order and keeps a trace of each. */
data class StrictZoneScheduleEngine(
    var steps: MutableList<StrictZoneScheduleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: StrictZoneSchedule): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<StrictZoneSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultStrictZoneScheduleEngine(): StrictZoneScheduleEngine {
    return StrictZoneScheduleEngine(
        steps = mutableListOf<StrictZoneScheduleModifier>(
            RegionStrictZoneScheduleAdjuster(mutableMapOf<String, Double>("central" to 1.22, "retail" to 0.72)),
            CapStrictZoneScheduleAdjuster(23928.0),
            MinimumStrictZoneScheduleAdjuster(33.1),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 34128.2,
    )
}
