package com.acme.transport.customs.declaration.overrideitem

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the declaration budget calculation. */
interface DeclarationBudgetAdjuster {
    fun describe(): String
    fun adjust(item: DeclarationBudget, value: Double): Double
}

data class TieredDeclarationBudgetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DeclarationBudgetAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DeclarationBudget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentDeclarationBudgetAdjuster(var percent: Double) : DeclarationBudgetAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DeclarationBudget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumDeclarationBudgetAdjuster(var minimum: Double) : DeclarationBudgetAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DeclarationBudget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapDeclarationBudgetAdjuster(var cap: Double) : DeclarationBudgetAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DeclarationBudget, value: Double): Double = minOf(value, cap)
}

data class DeclarationBudgetMaximumOffsetAdjuster(var weight: Double) : DeclarationBudgetAdjuster {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: DeclarationBudget, value: Double): Double = value + item.maximum * weight
}

data class RoundingDeclarationBudgetAdjuster(var step: Double) : DeclarationBudgetAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DeclarationBudget, value: Double): Double = roundTo(value, step)
}

/** Runs the declaration budget steps in order and keeps a trace of each. */
data class DeclarationBudgetEngine(
    var steps: MutableList<DeclarationBudgetAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DeclarationBudget): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DeclarationBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDeclarationBudgetEngine(): DeclarationBudgetEngine {
    return DeclarationBudgetEngine(
        steps = mutableListOf<DeclarationBudgetAdjuster>(
            TieredDeclarationBudgetAdjuster(
                mutableListOf<Double>(1665.0, 4962.0),
                mutableListOf<Double>(1.1, 0.96),
            ),
            PercentDeclarationBudgetAdjuster(15.5),
            MinimumDeclarationBudgetAdjuster(7.2),
            CapDeclarationBudgetAdjuster(42454.0),
            DeclarationBudgetMaximumOffsetAdjuster(1.56),
            RoundingDeclarationBudgetAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 80562.6,
    )
}
