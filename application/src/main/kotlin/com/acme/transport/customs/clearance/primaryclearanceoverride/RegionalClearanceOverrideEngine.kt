package com.acme.transport.customs.clearance.primaryclearanceoverride

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the regional clearance override calculation. */
interface RegionalClearanceOverrideModifier {
    fun describe(): String
    fun adjust(item: RegionalClearanceOverride, value: Double): Double
}

data class MinimumRegionalClearanceOverrideAdjuster(var minimum: Double) : RegionalClearanceOverrideModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalClearanceOverride, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentRegionalClearanceOverrideAdjuster(var percent: Double) : RegionalClearanceOverrideModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalClearanceOverride, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapRegionalClearanceOverrideAdjuster(var cap: Double) : RegionalClearanceOverrideModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalClearanceOverride, value: Double): Double = minOf(value, cap)
}

/** Runs the regional clearance override steps in order and keeps a trace of each. */
data class RegionalClearanceOverrideEngine(
    var steps: MutableList<RegionalClearanceOverrideModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RegionalClearanceOverride): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RegionalClearanceOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRegionalClearanceOverrideEngine(): RegionalClearanceOverrideEngine {
    return RegionalClearanceOverrideEngine(
        steps = mutableListOf<RegionalClearanceOverrideModifier>(
            MinimumRegionalClearanceOverrideAdjuster(31.9),
            PercentRegionalClearanceOverrideAdjuster(-3.3),
            CapRegionalClearanceOverrideAdjuster(22565.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 7714.0,
    )
}
