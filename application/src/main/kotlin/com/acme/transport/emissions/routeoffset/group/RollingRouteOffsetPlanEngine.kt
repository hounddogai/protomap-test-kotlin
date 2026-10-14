package com.acme.transport.emissions.routeoffset.group

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rolling route offset plan calculation. */
interface RollingRouteOffsetPlanStep {
    fun describe(): String
    fun adjust(item: RollingRouteOffsetPlan, value: Double): Double
}

data class TieredRollingRouteOffsetPlanAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingRouteOffsetPlanStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingRouteOffsetPlan, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumRollingRouteOffsetPlanAdjuster(var minimum: Double) : RollingRouteOffsetPlanStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingRouteOffsetPlan, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingRollingRouteOffsetPlanAdjuster(var step: Double) : RollingRouteOffsetPlanStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingRouteOffsetPlan, value: Double): Double = roundTo(value, step)
}

data class RollingRouteOffsetPlanEndsAtOffsetAdjuster(var weight: Double) : RollingRouteOffsetPlanStep {
    override fun describe(): String = "plus ends at times $weight"

    override fun adjust(item: RollingRouteOffsetPlan, value: Double): Double = value + item.endsAt.toDouble() * weight
}

data class PercentRollingRouteOffsetPlanAdjuster(var percent: Double) : RollingRouteOffsetPlanStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingRouteOffsetPlan, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapRollingRouteOffsetPlanAdjuster(var cap: Double) : RollingRouteOffsetPlanStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingRouteOffsetPlan, value: Double): Double = minOf(value, cap)
}

/** Runs the rolling route offset plan steps in order and keeps a trace of each. */
data class RollingRouteOffsetPlanEngine(
    var steps: MutableList<RollingRouteOffsetPlanStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RollingRouteOffsetPlan): Double {
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

    fun evaluateAll(items: MutableList<RollingRouteOffsetPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRollingRouteOffsetPlanEngine(): RollingRouteOffsetPlanEngine {
    return RollingRouteOffsetPlanEngine(
        steps = mutableListOf<RollingRouteOffsetPlanStep>(
            TieredRollingRouteOffsetPlanAdjuster(
                mutableListOf<Double>(2451.0, 9139.0),
                mutableListOf<Double>(1.16, 1.13),
            ),
            MinimumRollingRouteOffsetPlanAdjuster(38.6),
            RoundingRollingRouteOffsetPlanAdjuster(0.25),
            RollingRouteOffsetPlanEndsAtOffsetAdjuster(1.49),
            PercentRollingRouteOffsetPlanAdjuster(-21.2),
            CapRollingRouteOffsetPlanAdjuster(13113.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 49393.2,
    )
}
