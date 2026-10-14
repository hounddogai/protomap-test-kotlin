package com.acme.transport.customs.clearance.clearancemilestone

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the rolling clearance milestone calculation. */
interface RollingClearanceMilestoneAdjuster {
    fun describe(): String
    fun adjust(item: RollingClearanceMilestone, value: Double): Double
}

data class MinimumRollingClearanceMilestoneAdjuster(var minimum: Double) : RollingClearanceMilestoneAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingClearanceMilestone, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentRollingClearanceMilestoneAdjuster(var percent: Double) : RollingClearanceMilestoneAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingClearanceMilestone, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapRollingClearanceMilestoneAdjuster(var cap: Double) : RollingClearanceMilestoneAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingClearanceMilestone, value: Double): Double = minOf(value, cap)
}

/** Runs the rolling clearance milestone steps in order and keeps a trace of each. */
data class RollingClearanceMilestoneEngine(
    var steps: MutableList<RollingClearanceMilestoneAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RollingClearanceMilestone): Double {
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

    fun evaluateAll(items: MutableList<RollingClearanceMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRollingClearanceMilestoneEngine(): RollingClearanceMilestoneEngine {
    return RollingClearanceMilestoneEngine(
        steps = mutableListOf<RollingClearanceMilestoneAdjuster>(
            MinimumRollingClearanceMilestoneAdjuster(3.2),
            PercentRollingClearanceMilestoneAdjuster(15.6),
            CapRollingClearanceMilestoneAdjuster(24365.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 12378.2,
    )
}
