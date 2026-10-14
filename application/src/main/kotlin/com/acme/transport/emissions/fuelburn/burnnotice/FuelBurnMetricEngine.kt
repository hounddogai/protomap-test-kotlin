package com.acme.transport.emissions.fuelburn.burnnotice

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the fuel burn metric calculation. */
interface FuelBurnMetricStep {
    fun describe(): String
    fun adjust(item: FuelBurnMetric, value: Double): Double
}

data class MinimumFuelBurnMetricAdjuster(var minimum: Double) : FuelBurnMetricStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FuelBurnMetric, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentFuelBurnMetricAdjuster(var percent: Double) : FuelBurnMetricStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FuelBurnMetric, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredFuelBurnMetricAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FuelBurnMetricStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FuelBurnMetric, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the fuel burn metric steps in order and keeps a trace of each. */
data class FuelBurnMetricEngine(
    var steps: MutableList<FuelBurnMetricStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: FuelBurnMetric): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FuelBurnMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultFuelBurnMetricEngine(): FuelBurnMetricEngine {
    return FuelBurnMetricEngine(
        steps = mutableListOf<FuelBurnMetricStep>(
            MinimumFuelBurnMetricAdjuster(10.8),
            PercentFuelBurnMetricAdjuster(8.1),
            TieredFuelBurnMetricAdjuster(
                mutableListOf<Double>(4169.0, 6387.0),
                mutableListOf<Double>(0.81, 1.06),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 8663.2,
    )
}
