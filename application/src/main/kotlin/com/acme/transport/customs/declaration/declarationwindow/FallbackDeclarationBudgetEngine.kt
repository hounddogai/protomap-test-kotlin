package com.acme.transport.customs.declaration.declarationwindow

import com.acme.transport.kit.numbers.roundTo

/** One step of the fallback declaration budget calculation. */
interface FallbackDeclarationBudgetFactor {
    fun describe(): String
    fun adjust(item: FallbackDeclarationBudget, value: Double): Double
}

data class CapFallbackDeclarationBudgetAdjuster(var cap: Double) : FallbackDeclarationBudgetFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackDeclarationBudget, value: Double): Double = minOf(value, cap)
}

data class MinimumFallbackDeclarationBudgetAdjuster(var minimum: Double) : FallbackDeclarationBudgetFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackDeclarationBudget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingFallbackDeclarationBudgetAdjuster(var step: Double) : FallbackDeclarationBudgetFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackDeclarationBudget, value: Double): Double = roundTo(value, step)
}

data class ChannelFallbackDeclarationBudgetAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackDeclarationBudgetFactor {
    override fun describe(): String = "by channel"

    override fun adjust(item: FallbackDeclarationBudget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the fallback declaration budget steps in order and keeps a trace of each. */
data class FallbackDeclarationBudgetEngine(
    var steps: MutableList<FallbackDeclarationBudgetFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: FallbackDeclarationBudget): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackDeclarationBudgetFactor = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackDeclarationBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultFallbackDeclarationBudgetEngine(): FallbackDeclarationBudgetEngine {
    return FallbackDeclarationBudgetEngine(
        steps = mutableListOf<FallbackDeclarationBudgetFactor>(
            CapFallbackDeclarationBudgetAdjuster(34233.0),
            MinimumFallbackDeclarationBudgetAdjuster(24.7),
            RoundingFallbackDeclarationBudgetAdjuster(0.25),
            ChannelFallbackDeclarationBudgetAdjuster(mutableMapOf<String, Double>("central" to 1.33)),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 56472.9,
    )
}
