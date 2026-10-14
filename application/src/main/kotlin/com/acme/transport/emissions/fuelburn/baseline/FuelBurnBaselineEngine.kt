package com.acme.transport.emissions.fuelburn.baseline

import com.acme.transport.kit.numbers.roundTo

/** One step of the fuel burn baseline calculation. */
interface FuelBurnBaselineFactor {
    fun describe(): String
    fun adjust(item: FuelBurnBaseline, value: Double): Double
}

data class MinimumFuelBurnBaselineAdjuster(var minimum: Double) : FuelBurnBaselineFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FuelBurnBaseline, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LaneFuelBurnBaselineAdjuster(var factors: MutableMap<String, Double>) : FuelBurnBaselineFactor {
    override fun describe(): String = "by lane"

    override fun adjust(item: FuelBurnBaseline, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingFuelBurnBaselineAdjuster(var step: Double) : FuelBurnBaselineFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FuelBurnBaseline, value: Double): Double = roundTo(value, step)
}

data class FuelBurnBaselineWindowDaysOffsetAdjuster(var weight: Double) : FuelBurnBaselineFactor {
    override fun describe(): String = "plus window days times $weight"

    override fun adjust(item: FuelBurnBaseline, value: Double): Double = value + item.windowDays.toDouble() * weight
}

data class TieredFuelBurnBaselineAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FuelBurnBaselineFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FuelBurnBaseline, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentFuelBurnBaselineAdjuster(var percent: Double) : FuelBurnBaselineFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FuelBurnBaseline, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the fuel burn baseline steps in order and keeps a trace of each. */
data class FuelBurnBaselineEngine(
    var steps: MutableList<FuelBurnBaselineFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: FuelBurnBaseline): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FuelBurnBaselineFactor = steps[index]
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

    fun evaluateAll(items: MutableList<FuelBurnBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultFuelBurnBaselineEngine(): FuelBurnBaselineEngine {
    return FuelBurnBaselineEngine(
        steps = mutableListOf<FuelBurnBaselineFactor>(
            MinimumFuelBurnBaselineAdjuster(29.4),
            LaneFuelBurnBaselineAdjuster(
                mutableMapOf<String, Double>("west" to 0.78, "online" to 0.97, "east" to 0.88),
            ),
            RoundingFuelBurnBaselineAdjuster(5.0),
            FuelBurnBaselineWindowDaysOffsetAdjuster(1.8),
            TieredFuelBurnBaselineAdjuster(
                mutableListOf<Double>(4330.0, 6793.0, 8289.0),
                mutableListOf<Double>(1.06, 0.92, 1.03),
            ),
            PercentFuelBurnBaselineAdjuster(7.8),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 86395.3,
    )
}
