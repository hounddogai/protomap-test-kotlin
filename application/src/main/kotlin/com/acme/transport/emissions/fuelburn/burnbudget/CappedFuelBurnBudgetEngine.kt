package com.acme.transport.emissions.fuelburn.burnbudget

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the capped fuel burn budget calculation. */
interface CappedFuelBurnBudgetFactor {
    fun describe(): String
    fun adjust(item: CappedFuelBurnBudget, value: Double): Double
}

data class TieredCappedFuelBurnBudgetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedFuelBurnBudgetFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedFuelBurnBudget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumCappedFuelBurnBudgetAdjuster(var minimum: Double) : CappedFuelBurnBudgetFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedFuelBurnBudget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedCappedFuelBurnBudgetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedFuelBurnBudgetFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedFuelBurnBudget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapCappedFuelBurnBudgetAdjuster(var cap: Double) : CappedFuelBurnBudgetFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedFuelBurnBudget, value: Double): Double = minOf(value, cap)
}

/** Runs the capped fuel burn budget steps in order and keeps a trace of each. */
data class CappedFuelBurnBudgetEngine(
    var steps: MutableList<CappedFuelBurnBudgetFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CappedFuelBurnBudget): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedFuelBurnBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCappedFuelBurnBudgetEngine(flags: Registry): CappedFuelBurnBudgetEngine {
    return CappedFuelBurnBudgetEngine(
        steps = mutableListOf<CappedFuelBurnBudgetFactor>(
            TieredCappedFuelBurnBudgetAdjuster(
                mutableListOf<Double>(4558.0, 7325.0, 7661.0),
                mutableListOf<Double>(1.15, 1.02, 1.03),
            ),
            MinimumCappedFuelBurnBudgetAdjuster(39.8),
            FlaggedCappedFuelBurnBudgetAdjuster(flags, "capped-fuel-burn-budget-global", -1.5),
            CapCappedFuelBurnBudgetAdjuster(16613.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 24329.0,
    )
}
