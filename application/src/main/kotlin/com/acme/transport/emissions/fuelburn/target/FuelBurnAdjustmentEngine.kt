package com.acme.transport.emissions.fuelburn.target

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the fuel burn adjustment calculation. */
interface FuelBurnAdjustmentStep {
    fun describe(): String
    fun adjust(item: FuelBurnAdjustment, value: Double): Double
}

data class RoundingFuelBurnAdjustmentAdjuster(var step: Double) : FuelBurnAdjustmentStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FuelBurnAdjustment, value: Double): Double = roundTo(value, step)
}

data class FlaggedFuelBurnAdjustmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FuelBurnAdjustmentStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FuelBurnAdjustment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumFuelBurnAdjustmentAdjuster(var minimum: Double) : FuelBurnAdjustmentStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FuelBurnAdjustment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapFuelBurnAdjustmentAdjuster(var cap: Double) : FuelBurnAdjustmentStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FuelBurnAdjustment, value: Double): Double = minOf(value, cap)
}

/** Runs the fuel burn adjustment steps in order and keeps a trace of each. */
data class FuelBurnAdjustmentEngine(
    var steps: MutableList<FuelBurnAdjustmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: FuelBurnAdjustment): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FuelBurnAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultFuelBurnAdjustmentEngine(flags: Registry): FuelBurnAdjustmentEngine {
    return FuelBurnAdjustmentEngine(
        steps = mutableListOf<FuelBurnAdjustmentStep>(
            RoundingFuelBurnAdjustmentAdjuster(1.0),
            FlaggedFuelBurnAdjustmentAdjuster(flags, "fuel-burn-adjustment-strict", 0.5),
            MinimumFuelBurnAdjustmentAdjuster(2.7),
            CapFuelBurnAdjustmentAdjuster(43866.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 73516.2,
    )
}
