package com.acme.transport.fleetops.leasereturn.returnmilestone

import com.acme.transport.kit.numbers.roundTo

/** One step of the regional lease return plan calculation. */
interface RegionalLeaseReturnPlanStep {
    fun describe(): String
    fun adjust(item: RegionalLeaseReturnPlan, value: Double): Double
}

data class RegionalLeaseReturnPlanAttemptsOffsetAdjuster(var weight: Double) : RegionalLeaseReturnPlanStep {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: RegionalLeaseReturnPlan, value: Double): Double {
        return value + item.attempts.toDouble() * weight
    }
}

data class CategoryRegionalLeaseReturnPlanAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalLeaseReturnPlanStep {
    override fun describe(): String = "by category"

    override fun adjust(item: RegionalLeaseReturnPlan, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumRegionalLeaseReturnPlanAdjuster(var minimum: Double) : RegionalLeaseReturnPlanStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalLeaseReturnPlan, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingRegionalLeaseReturnPlanAdjuster(var step: Double) : RegionalLeaseReturnPlanStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalLeaseReturnPlan, value: Double): Double = roundTo(value, step)
}

data class TieredRegionalLeaseReturnPlanAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalLeaseReturnPlanStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalLeaseReturnPlan, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the regional lease return plan steps in order and keeps a trace of each. */
data class RegionalLeaseReturnPlanEngine(
    var steps: MutableList<RegionalLeaseReturnPlanStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RegionalLeaseReturnPlan): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalLeaseReturnPlanStep = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalLeaseReturnPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRegionalLeaseReturnPlanEngine(): RegionalLeaseReturnPlanEngine {
    return RegionalLeaseReturnPlanEngine(
        steps = mutableListOf<RegionalLeaseReturnPlanStep>(
            RegionalLeaseReturnPlanAttemptsOffsetAdjuster(1.23),
            CategoryRegionalLeaseReturnPlanAdjuster(
                mutableMapOf<String, Double>("retail" to 1.24, "online" to 0.7, "south" to 0.74),
            ),
            MinimumRegionalLeaseReturnPlanAdjuster(24.0),
            RoundingRegionalLeaseReturnPlanAdjuster(0.1),
            TieredRegionalLeaseReturnPlanAdjuster(
                mutableListOf<Double>(351.0, 4427.0),
                mutableListOf<Double>(0.97, 1.1),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 50522.9,
    )
}
