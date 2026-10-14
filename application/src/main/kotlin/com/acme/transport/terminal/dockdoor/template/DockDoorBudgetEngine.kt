package com.acme.transport.terminal.dockdoor.template

import com.acme.transport.kit.numbers.roundTo

/** One step of the dock door budget calculation. */
interface DockDoorBudgetAdjuster {
    fun describe(): String
    fun adjust(item: DockDoorBudget, value: Double): Double
}

data class RoundingDockDoorBudgetAdjuster(var step: Double) : DockDoorBudgetAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DockDoorBudget, value: Double): Double = roundTo(value, step)
}

data class PercentDockDoorBudgetAdjuster(var percent: Double) : DockDoorBudgetAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DockDoorBudget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LocaleDockDoorBudgetAdjuster(var factors: MutableMap<String, Double>) : DockDoorBudgetAdjuster {
    override fun describe(): String = "by locale"

    override fun adjust(item: DockDoorBudget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the dock door budget steps in order and keeps a trace of each. */
data class DockDoorBudgetEngine(
    var steps: MutableList<DockDoorBudgetAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DockDoorBudget): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DockDoorBudgetAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<DockDoorBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDockDoorBudgetEngine(): DockDoorBudgetEngine {
    return DockDoorBudgetEngine(
        steps = mutableListOf<DockDoorBudgetAdjuster>(
            RoundingDockDoorBudgetAdjuster(0.25),
            PercentDockDoorBudgetAdjuster(2.3),
            LocaleDockDoorBudgetAdjuster(mutableMapOf<String, Double>("east" to 1.36)),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 32326.9,
    )
}
