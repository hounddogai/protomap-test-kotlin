package com.acme.transport.emissions.fuelburn.profile

import com.acme.transport.kit.numbers.roundTo

/** One step of the fuel burn tier calculation. */
interface FuelBurnTierAdjuster {
    fun describe(): String
    fun adjust(item: FuelBurnTier, value: Double): Double
}

data class CountryFuelBurnTierAdjuster(var factors: MutableMap<String, Double>) : FuelBurnTierAdjuster {
    override fun describe(): String = "by country"

    override fun adjust(item: FuelBurnTier, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapFuelBurnTierAdjuster(var cap: Double) : FuelBurnTierAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FuelBurnTier, value: Double): Double = minOf(value, cap)
}

data class RoundingFuelBurnTierAdjuster(var step: Double) : FuelBurnTierAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FuelBurnTier, value: Double): Double = roundTo(value, step)
}

data class PercentFuelBurnTierAdjuster(var percent: Double) : FuelBurnTierAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FuelBurnTier, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the fuel burn tier steps in order and keeps a trace of each. */
data class FuelBurnTierEngine(
    var steps: MutableList<FuelBurnTierAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: FuelBurnTier): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FuelBurnTierAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<FuelBurnTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultFuelBurnTierEngine(): FuelBurnTierEngine {
    return FuelBurnTierEngine(
        steps = mutableListOf<FuelBurnTierAdjuster>(
            CountryFuelBurnTierAdjuster(mutableMapOf<String, Double>("west" to 0.77)),
            CapFuelBurnTierAdjuster(49711.0),
            RoundingFuelBurnTierAdjuster(0.05),
            PercentFuelBurnTierAdjuster(19.4),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 5616.0,
    )
}
