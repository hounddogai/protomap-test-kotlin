package com.acme.transport.crewing.roster.variance

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the roster plan calculation. */
interface RosterPlanFactor {
    fun describe(): String
    fun adjust(item: RosterPlan, value: Double): Double
}

data class RoundingRosterPlanAdjuster(var step: Double) : RosterPlanFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RosterPlan, value: Double): Double = roundTo(value, step)
}

data class RosterPlanPriorityOffsetAdjuster(var weight: Double) : RosterPlanFactor {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: RosterPlan, value: Double): Double = value + item.priority.toDouble() * weight
}

data class TieredRosterPlanAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RosterPlanFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RosterPlan, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapRosterPlanAdjuster(var cap: Double) : RosterPlanFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RosterPlan, value: Double): Double = minOf(value, cap)
}

/** Runs the roster plan steps in order and keeps a trace of each. */
data class RosterPlanEngine(
    var steps: MutableList<RosterPlanFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RosterPlan): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RosterPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRosterPlanEngine(): RosterPlanEngine {
    return RosterPlanEngine(
        steps = mutableListOf<RosterPlanFactor>(
            RoundingRosterPlanAdjuster(5.0),
            RosterPlanPriorityOffsetAdjuster(0.19),
            TieredRosterPlanAdjuster(
                mutableListOf<Double>(282.0, 1341.0, 5304.0),
                mutableListOf<Double>(1.09, 1.13, 0.9),
            ),
            CapRosterPlanAdjuster(12522.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 28956.0,
    )
}
