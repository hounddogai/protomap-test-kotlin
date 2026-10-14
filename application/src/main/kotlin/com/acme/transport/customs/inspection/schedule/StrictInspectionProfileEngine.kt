package com.acme.transport.customs.inspection.schedule

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the strict inspection profile calculation. */
interface StrictInspectionProfileModifier {
    fun describe(): String
    fun adjust(item: StrictInspectionProfile, value: Double): Double
}

data class NameStrictInspectionProfileAdjuster(
    var factors: MutableMap<String, Double>,
) : StrictInspectionProfileModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: StrictInspectionProfile, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumStrictInspectionProfileAdjuster(var minimum: Double) : StrictInspectionProfileModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictInspectionProfile, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapStrictInspectionProfileAdjuster(var cap: Double) : StrictInspectionProfileModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictInspectionProfile, value: Double): Double = minOf(value, cap)
}

/** Runs the strict inspection profile steps in order and keeps a trace of each. */
data class StrictInspectionProfileEngine(
    var steps: MutableList<StrictInspectionProfileModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: StrictInspectionProfile): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<StrictInspectionProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultStrictInspectionProfileEngine(): StrictInspectionProfileEngine {
    return StrictInspectionProfileEngine(
        steps = mutableListOf<StrictInspectionProfileModifier>(
            NameStrictInspectionProfileAdjuster(
                mutableMapOf<String, Double>("east" to 1.3, "south" to 0.78, "central" to 1.38),
            ),
            MinimumStrictInspectionProfileAdjuster(45.3),
            CapStrictInspectionProfileAdjuster(30584.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 73846.3,
    )
}
