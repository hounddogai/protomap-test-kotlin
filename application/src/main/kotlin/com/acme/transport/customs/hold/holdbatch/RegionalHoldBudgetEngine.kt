package com.acme.transport.customs.hold.holdbatch

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the regional hold budget calculation. */
interface RegionalHoldBudgetStep {
    fun describe(): String
    fun adjust(item: RegionalHoldBudget, value: Double): Double
}

data class PercentRegionalHoldBudgetAdjuster(var percent: Double) : RegionalHoldBudgetStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalHoldBudget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RegionalHoldBudgetFuelLitersOffsetAdjuster(var weight: Double) : RegionalHoldBudgetStep {
    override fun describe(): String = "plus fuel liters times $weight"

    override fun adjust(item: RegionalHoldBudget, value: Double): Double = value + item.fuelLiters * weight
}

data class FlaggedRegionalHoldBudgetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalHoldBudgetStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalHoldBudget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the regional hold budget steps in order and keeps a trace of each. */
data class RegionalHoldBudgetEngine(
    var steps: MutableList<RegionalHoldBudgetStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RegionalHoldBudget): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RegionalHoldBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRegionalHoldBudgetEngine(flags: Registry): RegionalHoldBudgetEngine {
    return RegionalHoldBudgetEngine(
        steps = mutableListOf<RegionalHoldBudgetStep>(
            PercentRegionalHoldBudgetAdjuster(1.8),
            RegionalHoldBudgetFuelLitersOffsetAdjuster(1.14),
            FlaggedRegionalHoldBudgetAdjuster(flags, "regional-hold-budget-legacy", -2.1),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 5863.7,
    )
}
