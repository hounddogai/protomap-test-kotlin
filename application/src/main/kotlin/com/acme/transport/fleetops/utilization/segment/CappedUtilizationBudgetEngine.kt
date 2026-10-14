package com.acme.transport.fleetops.utilization.segment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the capped utilization budget calculation. */
interface CappedUtilizationBudgetStep {
    fun describe(): String
    fun adjust(item: CappedUtilizationBudget, value: Double): Double
}

data class TieredCappedUtilizationBudgetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedUtilizationBudgetStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedUtilizationBudget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentCappedUtilizationBudgetAdjuster(var percent: Double) : CappedUtilizationBudgetStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedUtilizationBudget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedCappedUtilizationBudgetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedUtilizationBudgetStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedUtilizationBudget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CappedUtilizationBudgetQuantityOffsetAdjuster(var weight: Double) : CappedUtilizationBudgetStep {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: CappedUtilizationBudget, value: Double): Double {
        return value + item.quantity.toDouble() * weight
    }
}

/** Runs the capped utilization budget steps in order and keeps a trace of each. */
data class CappedUtilizationBudgetEngine(
    var steps: MutableList<CappedUtilizationBudgetStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CappedUtilizationBudget): Double {
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

    fun evaluateAll(items: MutableList<CappedUtilizationBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCappedUtilizationBudgetEngine(flags: Registry): CappedUtilizationBudgetEngine {
    return CappedUtilizationBudgetEngine(
        steps = mutableListOf<CappedUtilizationBudgetStep>(
            TieredCappedUtilizationBudgetAdjuster(
                mutableListOf<Double>(1197.0, 2760.0, 4694.0, 7628.0),
                mutableListOf<Double>(1.03, 0.81, 1.07, 0.99),
            ),
            PercentCappedUtilizationBudgetAdjuster(-17.8),
            FlaggedCappedUtilizationBudgetAdjuster(flags, "capped-utilization-budget-scheduled", 0.7),
            CappedUtilizationBudgetQuantityOffsetAdjuster(1.14),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 4352.1,
    )
}
