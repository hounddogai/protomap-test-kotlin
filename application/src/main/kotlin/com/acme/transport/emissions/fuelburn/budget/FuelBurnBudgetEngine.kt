package com.acme.transport.emissions.fuelburn.budget

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the fuel burn budget calculation. */
interface FuelBurnBudgetStep {
    fun describe(): String
    fun adjust(item: FuelBurnBudget, value: Double): Double
}

data class FuelBurnBudgetSequenceOffsetAdjuster(var weight: Double) : FuelBurnBudgetStep {
    override fun describe(): String = "plus sequence times $weight"

    override fun adjust(item: FuelBurnBudget, value: Double): Double = value + item.sequence.toDouble() * weight
}

data class TieredFuelBurnBudgetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FuelBurnBudgetStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FuelBurnBudget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapFuelBurnBudgetAdjuster(var cap: Double) : FuelBurnBudgetStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FuelBurnBudget, value: Double): Double = minOf(value, cap)
}

data class PercentFuelBurnBudgetAdjuster(var percent: Double) : FuelBurnBudgetStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FuelBurnBudget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingFuelBurnBudgetAdjuster(var step: Double) : FuelBurnBudgetStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FuelBurnBudget, value: Double): Double = roundTo(value, step)
}

data class MinimumFuelBurnBudgetAdjuster(var minimum: Double) : FuelBurnBudgetStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FuelBurnBudget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the fuel burn budget steps in order and keeps a trace of each. */
data class FuelBurnBudgetEngine(
    var steps: MutableList<FuelBurnBudgetStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: FuelBurnBudget): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FuelBurnBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultFuelBurnBudgetEngine(): FuelBurnBudgetEngine {
    return FuelBurnBudgetEngine(
        steps = mutableListOf<FuelBurnBudgetStep>(
            FuelBurnBudgetSequenceOffsetAdjuster(1.56),
            TieredFuelBurnBudgetAdjuster(
                mutableListOf<Double>(7661.0, 8290.0, 9355.0),
                mutableListOf<Double>(1.15, 0.97, 1.17),
            ),
            CapFuelBurnBudgetAdjuster(32216.0),
            PercentFuelBurnBudgetAdjuster(16.8),
            RoundingFuelBurnBudgetAdjuster(5.0),
            MinimumFuelBurnBudgetAdjuster(13.8),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 70292.2,
    )
}
