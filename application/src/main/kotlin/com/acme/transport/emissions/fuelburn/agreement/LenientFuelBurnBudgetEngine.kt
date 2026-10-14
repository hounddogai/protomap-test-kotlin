package com.acme.transport.emissions.fuelburn.agreement

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the lenient fuel burn budget calculation. */
interface LenientFuelBurnBudgetStep {
    fun describe(): String
    fun adjust(item: LenientFuelBurnBudget, value: Double): Double
}

data class TieredLenientFuelBurnBudgetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientFuelBurnBudgetStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientFuelBurnBudget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentLenientFuelBurnBudgetAdjuster(var percent: Double) : LenientFuelBurnBudgetStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientFuelBurnBudget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedLenientFuelBurnBudgetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientFuelBurnBudgetStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientFuelBurnBudget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LenientFuelBurnBudgetSequenceOffsetAdjuster(var weight: Double) : LenientFuelBurnBudgetStep {
    override fun describe(): String = "plus sequence times $weight"

    override fun adjust(item: LenientFuelBurnBudget, value: Double): Double {
        return value + item.sequence.toDouble() * weight
    }
}

data class CapLenientFuelBurnBudgetAdjuster(var cap: Double) : LenientFuelBurnBudgetStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientFuelBurnBudget, value: Double): Double = minOf(value, cap)
}

data class MinimumLenientFuelBurnBudgetAdjuster(var minimum: Double) : LenientFuelBurnBudgetStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientFuelBurnBudget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the lenient fuel burn budget steps in order and keeps a trace of each. */
data class LenientFuelBurnBudgetEngine(
    var steps: MutableList<LenientFuelBurnBudgetStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LenientFuelBurnBudget): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LenientFuelBurnBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLenientFuelBurnBudgetEngine(flags: Registry): LenientFuelBurnBudgetEngine {
    return LenientFuelBurnBudgetEngine(
        steps = mutableListOf<LenientFuelBurnBudgetStep>(
            TieredLenientFuelBurnBudgetAdjuster(
                mutableListOf<Double>(5383.0, 6126.0, 6263.0, 7858.0),
                mutableListOf<Double>(1.14, 1.0, 1.04, 1.02),
            ),
            PercentLenientFuelBurnBudgetAdjuster(-10.1),
            FlaggedLenientFuelBurnBudgetAdjuster(flags, "lenient-fuel-burn-budget-legacy", 7.8),
            LenientFuelBurnBudgetSequenceOffsetAdjuster(0.66),
            CapLenientFuelBurnBudgetAdjuster(24092.0),
            MinimumLenientFuelBurnBudgetAdjuster(26.4),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 87271.9,
    )
}
