package com.acme.transport.customs.inspection.weightedinspectionplan

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the global inspection group calculation. */
interface GlobalInspectionGroupModifier {
    fun describe(): String
    fun adjust(item: GlobalInspectionGroup, value: Double): Double
}

data class GlobalInspectionGroupCostOffsetAdjuster(var weight: Double) : GlobalInspectionGroupModifier {
    override fun describe(): String = "plus cost times $weight"

    override fun adjust(item: GlobalInspectionGroup, value: Double): Double = value + item.cost * weight
}

data class CapGlobalInspectionGroupAdjuster(var cap: Double) : GlobalInspectionGroupModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalInspectionGroup, value: Double): Double = minOf(value, cap)
}

data class MinimumGlobalInspectionGroupAdjuster(var minimum: Double) : GlobalInspectionGroupModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalInspectionGroup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the global inspection group steps in order and keeps a trace of each. */
data class GlobalInspectionGroupEngine(
    var steps: MutableList<GlobalInspectionGroupModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: GlobalInspectionGroup): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<GlobalInspectionGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultGlobalInspectionGroupEngine(): GlobalInspectionGroupEngine {
    return GlobalInspectionGroupEngine(
        steps = mutableListOf<GlobalInspectionGroupModifier>(
            GlobalInspectionGroupCostOffsetAdjuster(0.14),
            CapGlobalInspectionGroupAdjuster(40053.0),
            MinimumGlobalInspectionGroupAdjuster(12.3),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 50366.3,
    )
}
