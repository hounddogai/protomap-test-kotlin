package com.acme.transport.customs.clearance.tier

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the clearance tier calculation. */
interface ClearanceTierStep {
    fun describe(): String
    fun adjust(item: ClearanceTier, value: Double): Double
}

data class CapClearanceTierAdjuster(var cap: Double) : ClearanceTierStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ClearanceTier, value: Double): Double = minOf(value, cap)
}

data class RegionClearanceTierAdjuster(var factors: MutableMap<String, Double>) : ClearanceTierStep {
    override fun describe(): String = "by region"

    override fun adjust(item: ClearanceTier, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumClearanceTierAdjuster(var minimum: Double) : ClearanceTierStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ClearanceTier, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the clearance tier steps in order and keeps a trace of each. */
data class ClearanceTierEngine(
    var steps: MutableList<ClearanceTierStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ClearanceTier): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ClearanceTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultClearanceTierEngine(): ClearanceTierEngine {
    return ClearanceTierEngine(
        steps = mutableListOf<ClearanceTierStep>(
            CapClearanceTierAdjuster(3981.0),
            RegionClearanceTierAdjuster(mutableMapOf<String, Double>("online" to 1.13, "north" to 1.1)),
            MinimumClearanceTierAdjuster(25.2),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 44612.9,
    )
}
