package com.acme.transport.terminal.dwell.snapshot

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the global dwell budget calculation. */
interface GlobalDwellBudgetModifier {
    fun describe(): String
    fun adjust(item: GlobalDwellBudget, value: Double): Double
}

data class PercentGlobalDwellBudgetAdjuster(var percent: Double) : GlobalDwellBudgetModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalDwellBudget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class UnitGlobalDwellBudgetAdjuster(var factors: MutableMap<String, Double>) : GlobalDwellBudgetModifier {
    override fun describe(): String = "by unit"

    override fun adjust(item: GlobalDwellBudget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingGlobalDwellBudgetAdjuster(var step: Double) : GlobalDwellBudgetModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalDwellBudget, value: Double): Double = roundTo(value, step)
}

/** Runs the global dwell budget steps in order and keeps a trace of each. */
data class GlobalDwellBudgetEngine(
    var steps: MutableList<GlobalDwellBudgetModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: GlobalDwellBudget): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<GlobalDwellBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultGlobalDwellBudgetEngine(): GlobalDwellBudgetEngine {
    return GlobalDwellBudgetEngine(
        steps = mutableListOf<GlobalDwellBudgetModifier>(
            PercentGlobalDwellBudgetAdjuster(-14.5),
            UnitGlobalDwellBudgetAdjuster(
                mutableMapOf<String, Double>("online" to 0.92, "central" to 0.73, "retail" to 0.94),
            ),
            RoundingGlobalDwellBudgetAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 81861.6,
    )
}
