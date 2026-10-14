package com.acme.transport.emissions.fuelburn.group

import com.acme.transport.kit.flags.Registry

/** One step of the fuel burn window calculation. */
interface FuelBurnWindowModifier {
    fun describe(): String
    fun adjust(item: FuelBurnWindow, value: Double): Double
}

data class FuelBurnWindowMarginOffsetAdjuster(var weight: Double) : FuelBurnWindowModifier {
    override fun describe(): String = "plus margin times $weight"

    override fun adjust(item: FuelBurnWindow, value: Double): Double = value + item.margin * weight
}

data class CapFuelBurnWindowAdjuster(var cap: Double) : FuelBurnWindowModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FuelBurnWindow, value: Double): Double = minOf(value, cap)
}

data class FlaggedFuelBurnWindowAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FuelBurnWindowModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FuelBurnWindow, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredFuelBurnWindowAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FuelBurnWindowModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FuelBurnWindow, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumFuelBurnWindowAdjuster(var minimum: Double) : FuelBurnWindowModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FuelBurnWindow, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the fuel burn window steps in order and keeps a trace of each. */
data class FuelBurnWindowEngine(
    var steps: MutableList<FuelBurnWindowModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: FuelBurnWindow): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FuelBurnWindowModifier = steps[index]
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

    fun evaluateAll(items: MutableList<FuelBurnWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultFuelBurnWindowEngine(flags: Registry): FuelBurnWindowEngine {
    return FuelBurnWindowEngine(
        steps = mutableListOf<FuelBurnWindowModifier>(
            FuelBurnWindowMarginOffsetAdjuster(0.96),
            CapFuelBurnWindowAdjuster(42138.0),
            FlaggedFuelBurnWindowAdjuster(flags, "fuel-burn-window-strict", -5.2),
            TieredFuelBurnWindowAdjuster(
                mutableListOf<Double>(2240.0, 3013.0, 4588.0, 5383.0),
                mutableListOf<Double>(0.95, 1.13, 0.9, 1.11),
            ),
            MinimumFuelBurnWindowAdjuster(27.2),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 60814.3,
    )
}
