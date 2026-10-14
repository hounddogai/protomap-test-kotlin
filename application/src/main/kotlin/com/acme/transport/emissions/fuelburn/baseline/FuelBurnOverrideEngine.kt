package com.acme.transport.emissions.fuelburn.baseline

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the fuel burn override calculation. */
interface FuelBurnOverrideFactor {
    fun describe(): String
    fun adjust(item: FuelBurnOverride, value: Double): Double
}

data class MinimumFuelBurnOverrideAdjuster(var minimum: Double) : FuelBurnOverrideFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FuelBurnOverride, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapFuelBurnOverrideAdjuster(var cap: Double) : FuelBurnOverrideFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FuelBurnOverride, value: Double): Double = minOf(value, cap)
}

data class FlaggedFuelBurnOverrideAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FuelBurnOverrideFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FuelBurnOverride, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredFuelBurnOverrideAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FuelBurnOverrideFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FuelBurnOverride, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentFuelBurnOverrideAdjuster(var percent: Double) : FuelBurnOverrideFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FuelBurnOverride, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class NameFuelBurnOverrideAdjuster(var factors: MutableMap<String, Double>) : FuelBurnOverrideFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: FuelBurnOverride, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the fuel burn override steps in order and keeps a trace of each. */
data class FuelBurnOverrideEngine(
    var steps: MutableList<FuelBurnOverrideFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: FuelBurnOverride): Double {
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

    fun evaluateAll(items: MutableList<FuelBurnOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultFuelBurnOverrideEngine(flags: Registry): FuelBurnOverrideEngine {
    return FuelBurnOverrideEngine(
        steps = mutableListOf<FuelBurnOverrideFactor>(
            MinimumFuelBurnOverrideAdjuster(31.2),
            CapFuelBurnOverrideAdjuster(33411.0),
            FlaggedFuelBurnOverrideAdjuster(flags, "fuel-burn-override-weighted", -6.7),
            TieredFuelBurnOverrideAdjuster(
                mutableListOf<Double>(4165.0, 4987.0),
                mutableListOf<Double>(1.19, 0.96),
            ),
            PercentFuelBurnOverrideAdjuster(-22.2),
            NameFuelBurnOverrideAdjuster(mutableMapOf<String, Double>("north" to 1.31, "south" to 1.05)),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 15465.0,
    )
}
