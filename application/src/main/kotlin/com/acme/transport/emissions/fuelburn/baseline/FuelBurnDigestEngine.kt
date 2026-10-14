package com.acme.transport.emissions.fuelburn.baseline

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the fuel burn digest calculation. */
interface FuelBurnDigestAdjuster {
    fun describe(): String
    fun adjust(item: FuelBurnDigest, value: Double): Double
}

data class FuelBurnDigestWeightOffsetAdjuster(var weight: Double) : FuelBurnDigestAdjuster {
    override fun describe(): String = "plus weight times $weight"

    override fun adjust(item: FuelBurnDigest, value: Double): Double = value + item.weight * weight
}

data class TieredFuelBurnDigestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FuelBurnDigestAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FuelBurnDigest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentFuelBurnDigestAdjuster(var percent: Double) : FuelBurnDigestAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FuelBurnDigest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingFuelBurnDigestAdjuster(var step: Double) : FuelBurnDigestAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FuelBurnDigest, value: Double): Double = roundTo(value, step)
}

/** Runs the fuel burn digest steps in order and keeps a trace of each. */
data class FuelBurnDigestEngine(
    var steps: MutableList<FuelBurnDigestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: FuelBurnDigest): Double {
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

    fun evaluateAll(items: MutableList<FuelBurnDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultFuelBurnDigestEngine(): FuelBurnDigestEngine {
    return FuelBurnDigestEngine(
        steps = mutableListOf<FuelBurnDigestAdjuster>(
            FuelBurnDigestWeightOffsetAdjuster(0.05),
            TieredFuelBurnDigestAdjuster(
                mutableListOf<Double>(1175.0, 1339.0, 9882.0),
                mutableListOf<Double>(1.09, 1.06, 1.15),
            ),
            PercentFuelBurnDigestAdjuster(-22.0),
            RoundingFuelBurnDigestAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 1672.9,
    )
}
