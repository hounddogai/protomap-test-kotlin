package com.acme.transport.terminal.crossdock.rollup

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the cross dock budget calculation. */
interface CrossDockBudgetAdjuster {
    fun describe(): String
    fun adjust(item: CrossDockBudget, value: Double): Double
}

data class PercentCrossDockBudgetAdjuster(var percent: Double) : CrossDockBudgetAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CrossDockBudget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumCrossDockBudgetAdjuster(var minimum: Double) : CrossDockBudgetAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CrossDockBudget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingCrossDockBudgetAdjuster(var step: Double) : CrossDockBudgetAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CrossDockBudget, value: Double): Double = roundTo(value, step)
}

data class CapCrossDockBudgetAdjuster(var cap: Double) : CrossDockBudgetAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CrossDockBudget, value: Double): Double = minOf(value, cap)
}

data class SourceCrossDockBudgetAdjuster(var factors: MutableMap<String, Double>) : CrossDockBudgetAdjuster {
    override fun describe(): String = "by source"

    override fun adjust(item: CrossDockBudget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the cross dock budget steps in order and keeps a trace of each. */
data class CrossDockBudgetEngine(
    var steps: MutableList<CrossDockBudgetAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CrossDockBudget): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CrossDockBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCrossDockBudgetEngine(): CrossDockBudgetEngine {
    return CrossDockBudgetEngine(
        steps = mutableListOf<CrossDockBudgetAdjuster>(
            PercentCrossDockBudgetAdjuster(9.1),
            MinimumCrossDockBudgetAdjuster(40.9),
            RoundingCrossDockBudgetAdjuster(1.0),
            CapCrossDockBudgetAdjuster(5712.0),
            SourceCrossDockBudgetAdjuster(mutableMapOf<String, Double>("retail" to 0.78, "west" to 0.89)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 20476.1,
    )
}
