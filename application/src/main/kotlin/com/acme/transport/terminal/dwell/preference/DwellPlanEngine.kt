package com.acme.transport.terminal.dwell.preference

import com.acme.transport.kit.numbers.roundTo

/** One step of the dwell plan calculation. */
interface DwellPlanModifier {
    fun describe(): String
    fun adjust(item: DwellPlan, value: Double): Double
}

data class DwellPlanCostOffsetAdjuster(var weight: Double) : DwellPlanModifier {
    override fun describe(): String = "plus cost times $weight"

    override fun adjust(item: DwellPlan, value: Double): Double = value + item.cost * weight
}

data class TieredDwellPlanAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DwellPlanModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DwellPlan, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingDwellPlanAdjuster(var step: Double) : DwellPlanModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DwellPlan, value: Double): Double = roundTo(value, step)
}

data class CapDwellPlanAdjuster(var cap: Double) : DwellPlanModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DwellPlan, value: Double): Double = minOf(value, cap)
}

/** Runs the dwell plan steps in order and keeps a trace of each. */
data class DwellPlanEngine(
    var steps: MutableList<DwellPlanModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DwellPlan): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DwellPlanModifier = steps[index]
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

    fun evaluateAll(items: MutableList<DwellPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDwellPlanEngine(): DwellPlanEngine {
    return DwellPlanEngine(
        steps = mutableListOf<DwellPlanModifier>(
            DwellPlanCostOffsetAdjuster(1.58),
            TieredDwellPlanAdjuster(
                mutableListOf<Double>(1655.0, 2939.0, 3151.0),
                mutableListOf<Double>(1.04, 0.84, 1.0),
            ),
            RoundingDwellPlanAdjuster(5.0),
            CapDwellPlanAdjuster(8628.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 3746.5,
    )
}
