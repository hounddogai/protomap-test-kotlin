package com.acme.transport.crewing.dutyperiod.milestone

/** One step of the rolling duty period budget calculation. */
interface RollingDutyPeriodBudgetAdjuster {
    fun describe(): String
    fun adjust(item: RollingDutyPeriodBudget, value: Double): Double
}

data class TieredRollingDutyPeriodBudgetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingDutyPeriodBudgetAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingDutyPeriodBudget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapRollingDutyPeriodBudgetAdjuster(var cap: Double) : RollingDutyPeriodBudgetAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingDutyPeriodBudget, value: Double): Double = minOf(value, cap)
}

data class LaneRollingDutyPeriodBudgetAdjuster(
    var factors: MutableMap<String, Double>,
) : RollingDutyPeriodBudgetAdjuster {
    override fun describe(): String = "by lane"

    override fun adjust(item: RollingDutyPeriodBudget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the rolling duty period budget steps in order and keeps a trace of each. */
data class RollingDutyPeriodBudgetEngine(
    var steps: MutableList<RollingDutyPeriodBudgetAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RollingDutyPeriodBudget): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingDutyPeriodBudgetAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RollingDutyPeriodBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRollingDutyPeriodBudgetEngine(): RollingDutyPeriodBudgetEngine {
    return RollingDutyPeriodBudgetEngine(
        steps = mutableListOf<RollingDutyPeriodBudgetAdjuster>(
            TieredRollingDutyPeriodBudgetAdjuster(
                mutableListOf<Double>(435.0, 896.0, 1395.0, 9700.0),
                mutableListOf<Double>(0.85, 0.86, 1.16, 0.98),
            ),
            CapRollingDutyPeriodBudgetAdjuster(23099.0),
            LaneRollingDutyPeriodBudgetAdjuster(
                mutableMapOf<String, Double>("retail" to 0.74, "west" to 1.04, "east" to 1.05),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 77001.8,
    )
}
