package com.acme.transport.fleetops.utilization.budget

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rolling utilization budget calculation. */
interface RollingUtilizationBudgetAdjuster {
    fun describe(): String
    fun adjust(item: RollingUtilizationBudget, value: Double): Double
}

data class RoundingRollingUtilizationBudgetAdjuster(var step: Double) : RollingUtilizationBudgetAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingUtilizationBudget, value: Double): Double = roundTo(value, step)
}

data class LaneRollingUtilizationBudgetAdjuster(
    var factors: MutableMap<String, Double>,
) : RollingUtilizationBudgetAdjuster {
    override fun describe(): String = "by lane"

    override fun adjust(item: RollingUtilizationBudget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RollingUtilizationBudgetOdometerOffsetAdjuster(
    var weight: Double,
) : RollingUtilizationBudgetAdjuster {
    override fun describe(): String = "plus odometer times $weight"

    override fun adjust(item: RollingUtilizationBudget, value: Double): Double {
        return value + item.odometer.toDouble() * weight
    }
}

data class CapRollingUtilizationBudgetAdjuster(var cap: Double) : RollingUtilizationBudgetAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingUtilizationBudget, value: Double): Double = minOf(value, cap)
}

data class MinimumRollingUtilizationBudgetAdjuster(var minimum: Double) : RollingUtilizationBudgetAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingUtilizationBudget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the rolling utilization budget steps in order and keeps a trace of each. */
data class RollingUtilizationBudgetEngine(
    var steps: MutableList<RollingUtilizationBudgetAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RollingUtilizationBudget): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RollingUtilizationBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRollingUtilizationBudgetEngine(): RollingUtilizationBudgetEngine {
    return RollingUtilizationBudgetEngine(
        steps = mutableListOf<RollingUtilizationBudgetAdjuster>(
            RoundingRollingUtilizationBudgetAdjuster(5.0),
            LaneRollingUtilizationBudgetAdjuster(mutableMapOf<String, Double>("online" to 1.25, "west" to 1.0)),
            RollingUtilizationBudgetOdometerOffsetAdjuster(1.89),
            CapRollingUtilizationBudgetAdjuster(36093.0),
            MinimumRollingUtilizationBudgetAdjuster(39.1),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 75961.0,
    )
}
