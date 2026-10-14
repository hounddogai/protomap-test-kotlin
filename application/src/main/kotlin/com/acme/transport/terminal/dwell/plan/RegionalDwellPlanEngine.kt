package com.acme.transport.terminal.dwell.plan

import com.acme.transport.kit.numbers.roundTo

/** One step of the regional dwell plan calculation. */
interface RegionalDwellPlanModifier {
    fun describe(): String
    fun adjust(item: RegionalDwellPlan, value: Double): Double
}

data class PercentRegionalDwellPlanAdjuster(var percent: Double) : RegionalDwellPlanModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalDwellPlan, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RegionalDwellPlanPriorityOffsetAdjuster(var weight: Double) : RegionalDwellPlanModifier {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: RegionalDwellPlan, value: Double): Double = value + item.priority.toDouble() * weight
}

data class TitleRegionalDwellPlanAdjuster(var factors: MutableMap<String, Double>) : RegionalDwellPlanModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: RegionalDwellPlan, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumRegionalDwellPlanAdjuster(var minimum: Double) : RegionalDwellPlanModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalDwellPlan, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredRegionalDwellPlanAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalDwellPlanModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalDwellPlan, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingRegionalDwellPlanAdjuster(var step: Double) : RegionalDwellPlanModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalDwellPlan, value: Double): Double = roundTo(value, step)
}

/** Runs the regional dwell plan steps in order and keeps a trace of each. */
data class RegionalDwellPlanEngine(
    var steps: MutableList<RegionalDwellPlanModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RegionalDwellPlan): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalDwellPlanModifier = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalDwellPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRegionalDwellPlanEngine(): RegionalDwellPlanEngine {
    return RegionalDwellPlanEngine(
        steps = mutableListOf<RegionalDwellPlanModifier>(
            PercentRegionalDwellPlanAdjuster(9.2),
            RegionalDwellPlanPriorityOffsetAdjuster(1.76),
            TitleRegionalDwellPlanAdjuster(
                mutableMapOf<String, Double>("west" to 1.17, "south" to 0.88, "online" to 0.71),
            ),
            MinimumRegionalDwellPlanAdjuster(17.9),
            TieredRegionalDwellPlanAdjuster(
                mutableListOf<Double>(1488.0, 3397.0, 3843.0, 3910.0),
                mutableListOf<Double>(0.96, 1.09, 0.86, 0.99),
            ),
            RoundingRegionalDwellPlanAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 92708.5,
    )
}
