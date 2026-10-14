package com.acme.transport.fleetops.utilization.proposal

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the utilization budget calculation. */
interface UtilizationBudgetAdjuster {
    fun describe(): String
    fun adjust(item: UtilizationBudget, value: Double): Double
}

data class CategoryUtilizationBudgetAdjuster(
    var factors: MutableMap<String, Double>,
) : UtilizationBudgetAdjuster {
    override fun describe(): String = "by category"

    override fun adjust(item: UtilizationBudget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingUtilizationBudgetAdjuster(var step: Double) : UtilizationBudgetAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: UtilizationBudget, value: Double): Double = roundTo(value, step)
}

data class MinimumUtilizationBudgetAdjuster(var minimum: Double) : UtilizationBudgetAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: UtilizationBudget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapUtilizationBudgetAdjuster(var cap: Double) : UtilizationBudgetAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: UtilizationBudget, value: Double): Double = minOf(value, cap)
}

data class PercentUtilizationBudgetAdjuster(var percent: Double) : UtilizationBudgetAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: UtilizationBudget, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the utilization budget steps in order and keeps a trace of each. */
data class UtilizationBudgetEngine(
    var steps: MutableList<UtilizationBudgetAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: UtilizationBudget): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<UtilizationBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultUtilizationBudgetEngine(): UtilizationBudgetEngine {
    return UtilizationBudgetEngine(
        steps = mutableListOf<UtilizationBudgetAdjuster>(
            CategoryUtilizationBudgetAdjuster(mutableMapOf<String, Double>("retail" to 0.71, "north" to 1.08)),
            RoundingUtilizationBudgetAdjuster(0.1),
            MinimumUtilizationBudgetAdjuster(21.7),
            CapUtilizationBudgetAdjuster(43079.0),
            PercentUtilizationBudgetAdjuster(-2.1),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 72433.4,
    )
}
