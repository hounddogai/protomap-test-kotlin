package com.acme.transport.safety.nearmiss.claim

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the default near miss budget calculation. */
interface DefaultNearMissBudgetModifier {
    fun describe(): String
    fun adjust(item: DefaultNearMissBudget, value: Double): Double
}

data class RoundingDefaultNearMissBudgetAdjuster(var step: Double) : DefaultNearMissBudgetModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultNearMissBudget, value: Double): Double = roundTo(value, step)
}

data class MinimumDefaultNearMissBudgetAdjuster(var minimum: Double) : DefaultNearMissBudgetModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultNearMissBudget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapDefaultNearMissBudgetAdjuster(var cap: Double) : DefaultNearMissBudgetModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultNearMissBudget, value: Double): Double = minOf(value, cap)
}

/** Runs the default near miss budget steps in order and keeps a trace of each. */
data class DefaultNearMissBudgetEngine(
    var steps: MutableList<DefaultNearMissBudgetModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DefaultNearMissBudget): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DefaultNearMissBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDefaultNearMissBudgetEngine(): DefaultNearMissBudgetEngine {
    return DefaultNearMissBudgetEngine(
        steps = mutableListOf<DefaultNearMissBudgetModifier>(
            RoundingDefaultNearMissBudgetAdjuster(0.1),
            MinimumDefaultNearMissBudgetAdjuster(8.3),
            CapDefaultNearMissBudgetAdjuster(14421.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 80852.7,
    )
}
