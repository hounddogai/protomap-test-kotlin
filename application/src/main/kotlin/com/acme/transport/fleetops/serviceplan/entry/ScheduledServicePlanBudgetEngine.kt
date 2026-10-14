package com.acme.transport.fleetops.serviceplan.entry

import com.acme.transport.kit.numbers.roundTo

/** One step of the scheduled service plan budget calculation. */
interface ScheduledServicePlanBudgetFactor {
    fun describe(): String
    fun adjust(item: ScheduledServicePlanBudget, value: Double): Double
}

data class TieredScheduledServicePlanBudgetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledServicePlanBudgetFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledServicePlanBudget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapScheduledServicePlanBudgetAdjuster(var cap: Double) : ScheduledServicePlanBudgetFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledServicePlanBudget, value: Double): Double = minOf(value, cap)
}

data class RegionScheduledServicePlanBudgetAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledServicePlanBudgetFactor {
    override fun describe(): String = "by region"

    override fun adjust(item: ScheduledServicePlanBudget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class ScheduledServicePlanBudgetStopCountOffsetAdjuster(
    var weight: Double,
) : ScheduledServicePlanBudgetFactor {
    override fun describe(): String = "plus stop count times $weight"

    override fun adjust(item: ScheduledServicePlanBudget, value: Double): Double {
        return value + item.stopCount.toDouble() * weight
    }
}

data class RoundingScheduledServicePlanBudgetAdjuster(var step: Double) : ScheduledServicePlanBudgetFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledServicePlanBudget, value: Double): Double = roundTo(value, step)
}

data class MinimumScheduledServicePlanBudgetAdjuster(var minimum: Double) : ScheduledServicePlanBudgetFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledServicePlanBudget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the scheduled service plan budget steps in order and keeps a trace of each. */
data class ScheduledServicePlanBudgetEngine(
    var steps: MutableList<ScheduledServicePlanBudgetFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ScheduledServicePlanBudget): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledServicePlanBudgetFactor = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledServicePlanBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultScheduledServicePlanBudgetEngine(): ScheduledServicePlanBudgetEngine {
    return ScheduledServicePlanBudgetEngine(
        steps = mutableListOf<ScheduledServicePlanBudgetFactor>(
            TieredScheduledServicePlanBudgetAdjuster(
                mutableListOf<Double>(4562.0, 6887.0),
                mutableListOf<Double>(0.98, 1.08),
            ),
            CapScheduledServicePlanBudgetAdjuster(48116.0),
            RegionScheduledServicePlanBudgetAdjuster(
                mutableMapOf<String, Double>("online" to 1.21, "east" to 1.08),
            ),
            ScheduledServicePlanBudgetStopCountOffsetAdjuster(1.97),
            RoundingScheduledServicePlanBudgetAdjuster(0.25),
            MinimumScheduledServicePlanBudgetAdjuster(35.1),
        ),
        trace = mutableListOf<String>(),
        floor = 0.0,
        ceiling = 71040.5,
    )
}
