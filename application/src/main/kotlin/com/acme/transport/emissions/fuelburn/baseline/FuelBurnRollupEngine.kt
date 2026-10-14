package com.acme.transport.emissions.fuelburn.baseline

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the fuel burn rollup calculation. */
interface FuelBurnRollupAdjuster {
    fun describe(): String
    fun adjust(item: FuelBurnRollup, value: Double): Double
}

data class CapFuelBurnRollupAdjuster(var cap: Double) : FuelBurnRollupAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FuelBurnRollup, value: Double): Double = minOf(value, cap)
}

data class CountryFuelBurnRollupAdjuster(var factors: MutableMap<String, Double>) : FuelBurnRollupAdjuster {
    override fun describe(): String = "by country"

    override fun adjust(item: FuelBurnRollup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FuelBurnRollupWeightOffsetAdjuster(var weight: Double) : FuelBurnRollupAdjuster {
    override fun describe(): String = "plus weight times $weight"

    override fun adjust(item: FuelBurnRollup, value: Double): Double = value + item.weight * weight
}

data class RoundingFuelBurnRollupAdjuster(var step: Double) : FuelBurnRollupAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FuelBurnRollup, value: Double): Double = roundTo(value, step)
}

data class MinimumFuelBurnRollupAdjuster(var minimum: Double) : FuelBurnRollupAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FuelBurnRollup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the fuel burn rollup steps in order and keeps a trace of each. */
data class FuelBurnRollupEngine(
    var steps: MutableList<FuelBurnRollupAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: FuelBurnRollup): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FuelBurnRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultFuelBurnRollupEngine(): FuelBurnRollupEngine {
    return FuelBurnRollupEngine(
        steps = mutableListOf<FuelBurnRollupAdjuster>(
            CapFuelBurnRollupAdjuster(19447.0),
            CountryFuelBurnRollupAdjuster(
                mutableMapOf<String, Double>("online" to 1.27, "north" to 1.3, "south" to 0.9),
            ),
            FuelBurnRollupWeightOffsetAdjuster(1.97),
            RoundingFuelBurnRollupAdjuster(0.25),
            MinimumFuelBurnRollupAdjuster(32.7),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 54576.2,
    )
}
