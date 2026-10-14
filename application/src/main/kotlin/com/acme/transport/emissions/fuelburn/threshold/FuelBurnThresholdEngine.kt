package com.acme.transport.emissions.fuelburn.threshold

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the fuel burn threshold calculation. */
interface FuelBurnThresholdModifier {
    fun describe(): String
    fun adjust(item: FuelBurnThreshold, value: Double): Double
}

data class FlaggedFuelBurnThresholdAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FuelBurnThresholdModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FuelBurnThreshold, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class FuelBurnThresholdBudgetOffsetAdjuster(var weight: Double) : FuelBurnThresholdModifier {
    override fun describe(): String = "plus budget times $weight"

    override fun adjust(item: FuelBurnThreshold, value: Double): Double = value + item.budget * weight
}

data class CapFuelBurnThresholdAdjuster(var cap: Double) : FuelBurnThresholdModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FuelBurnThreshold, value: Double): Double = minOf(value, cap)
}

data class PercentFuelBurnThresholdAdjuster(var percent: Double) : FuelBurnThresholdModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FuelBurnThreshold, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the fuel burn threshold steps in order and keeps a trace of each. */
data class FuelBurnThresholdEngine(
    var steps: MutableList<FuelBurnThresholdModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: FuelBurnThreshold): Double {
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

    fun evaluateAll(items: MutableList<FuelBurnThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultFuelBurnThresholdEngine(flags: Registry): FuelBurnThresholdEngine {
    return FuelBurnThresholdEngine(
        steps = mutableListOf<FuelBurnThresholdModifier>(
            FlaggedFuelBurnThresholdAdjuster(flags, "fuel-burn-threshold-regional", -2.8),
            FuelBurnThresholdBudgetOffsetAdjuster(1.64),
            CapFuelBurnThresholdAdjuster(13333.0),
            PercentFuelBurnThresholdAdjuster(23.7),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 73980.1,
    )
}
