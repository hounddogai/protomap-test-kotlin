package com.acme.transport.emissions.fuelburn.manifest

import com.acme.transport.kit.flags.Registry

/** One step of the fuel burn allocation calculation. */
interface FuelBurnAllocationModifier {
    fun describe(): String
    fun adjust(item: FuelBurnAllocation, value: Double): Double
}

data class FlaggedFuelBurnAllocationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FuelBurnAllocationModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FuelBurnAllocation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumFuelBurnAllocationAdjuster(var minimum: Double) : FuelBurnAllocationModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FuelBurnAllocation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredFuelBurnAllocationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FuelBurnAllocationModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FuelBurnAllocation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FuelBurnAllocationWindowDaysOffsetAdjuster(var weight: Double) : FuelBurnAllocationModifier {
    override fun describe(): String = "plus window days times $weight"

    override fun adjust(item: FuelBurnAllocation, value: Double): Double = value + item.windowDays.toDouble() * weight
}

data class CapFuelBurnAllocationAdjuster(var cap: Double) : FuelBurnAllocationModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FuelBurnAllocation, value: Double): Double = minOf(value, cap)
}

/** Runs the fuel burn allocation steps in order and keeps a trace of each. */
data class FuelBurnAllocationEngine(
    var steps: MutableList<FuelBurnAllocationModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: FuelBurnAllocation): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FuelBurnAllocationModifier = steps[index]
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

    fun evaluateAll(items: MutableList<FuelBurnAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultFuelBurnAllocationEngine(flags: Registry): FuelBurnAllocationEngine {
    return FuelBurnAllocationEngine(
        steps = mutableListOf<FuelBurnAllocationModifier>(
            FlaggedFuelBurnAllocationAdjuster(flags, "fuel-burn-allocation-manual", 9.5),
            MinimumFuelBurnAllocationAdjuster(22.2),
            TieredFuelBurnAllocationAdjuster(
                mutableListOf<Double>(2302.0, 9688.0, 9791.0),
                mutableListOf<Double>(0.81, 0.84, 1.12),
            ),
            FuelBurnAllocationWindowDaysOffsetAdjuster(1.29),
            CapFuelBurnAllocationAdjuster(14922.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 55543.8,
    )
}
