package com.acme.transport.safety.incident.incidenttarget

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rolling incident baseline calculation. */
interface RollingIncidentBaselineModifier {
    fun describe(): String
    fun adjust(item: RollingIncidentBaseline, value: Double): Double
}

data class MinimumRollingIncidentBaselineAdjuster(var minimum: Double) : RollingIncidentBaselineModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingIncidentBaseline, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RegionRollingIncidentBaselineAdjuster(
    var factors: MutableMap<String, Double>,
) : RollingIncidentBaselineModifier {
    override fun describe(): String = "by region"

    override fun adjust(item: RollingIncidentBaseline, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingRollingIncidentBaselineAdjuster(var step: Double) : RollingIncidentBaselineModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingIncidentBaseline, value: Double): Double = roundTo(value, step)
}

data class RollingIncidentBaselineLoadKgOffsetAdjuster(var weight: Double) : RollingIncidentBaselineModifier {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: RollingIncidentBaseline, value: Double): Double = value + item.loadKg * weight
}

/** Runs the rolling incident baseline steps in order and keeps a trace of each. */
data class RollingIncidentBaselineEngine(
    var steps: MutableList<RollingIncidentBaselineModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RollingIncidentBaseline): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RollingIncidentBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRollingIncidentBaselineEngine(): RollingIncidentBaselineEngine {
    return RollingIncidentBaselineEngine(
        steps = mutableListOf<RollingIncidentBaselineModifier>(
            MinimumRollingIncidentBaselineAdjuster(7.3),
            RegionRollingIncidentBaselineAdjuster(
                mutableMapOf<String, Double>("central" to 0.88, "south" to 1.25, "online" to 1.12),
            ),
            RoundingRollingIncidentBaselineAdjuster(0.5),
            RollingIncidentBaselineLoadKgOffsetAdjuster(0.17),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 12962.5,
    )
}
