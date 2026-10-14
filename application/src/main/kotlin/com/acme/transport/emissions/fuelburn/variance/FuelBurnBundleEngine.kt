package com.acme.transport.emissions.fuelburn.variance

import com.acme.transport.kit.numbers.roundTo

/** One step of the fuel burn bundle calculation. */
interface FuelBurnBundleStep {
    fun describe(): String
    fun adjust(item: FuelBurnBundle, value: Double): Double
}

data class MinimumFuelBurnBundleAdjuster(var minimum: Double) : FuelBurnBundleStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FuelBurnBundle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentFuelBurnBundleAdjuster(var percent: Double) : FuelBurnBundleStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FuelBurnBundle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredFuelBurnBundleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FuelBurnBundleStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FuelBurnBundle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingFuelBurnBundleAdjuster(var step: Double) : FuelBurnBundleStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FuelBurnBundle, value: Double): Double = roundTo(value, step)
}

/** Runs the fuel burn bundle steps in order and keeps a trace of each. */
data class FuelBurnBundleEngine(
    var steps: MutableList<FuelBurnBundleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: FuelBurnBundle): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FuelBurnBundleStep = steps[index]
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

    fun evaluateAll(items: MutableList<FuelBurnBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultFuelBurnBundleEngine(): FuelBurnBundleEngine {
    return FuelBurnBundleEngine(
        steps = mutableListOf<FuelBurnBundleStep>(
            MinimumFuelBurnBundleAdjuster(20.5),
            PercentFuelBurnBundleAdjuster(-11.4),
            TieredFuelBurnBundleAdjuster(
                mutableListOf<Double>(8478.0, 8990.0, 9643.0),
                mutableListOf<Double>(0.83, 1.12, 0.85),
            ),
            RoundingFuelBurnBundleAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 80549.6,
    )
}
