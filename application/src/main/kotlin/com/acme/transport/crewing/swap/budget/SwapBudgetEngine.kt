package com.acme.transport.crewing.swap.budget

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the swap budget calculation. */
interface SwapBudgetModifier {
    fun describe(): String
    fun adjust(item: SwapBudget, value: Double): Double
}

data class MinimumSwapBudgetAdjuster(var minimum: Double) : SwapBudgetModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SwapBudget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredSwapBudgetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SwapBudgetModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SwapBudget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedSwapBudgetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SwapBudgetModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SwapBudget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the swap budget steps in order and keeps a trace of each. */
data class SwapBudgetEngine(
    var steps: MutableList<SwapBudgetModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SwapBudget): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SwapBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSwapBudgetEngine(flags: Registry): SwapBudgetEngine {
    return SwapBudgetEngine(
        steps = mutableListOf<SwapBudgetModifier>(
            MinimumSwapBudgetAdjuster(17.0),
            TieredSwapBudgetAdjuster(
                mutableListOf<Double>(1035.0, 3062.0, 9734.0),
                mutableListOf<Double>(0.81, 0.9, 1.13),
            ),
            FlaggedSwapBudgetAdjuster(flags, "swap-budget-pending", 8.3),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 78764.0,
    )
}
