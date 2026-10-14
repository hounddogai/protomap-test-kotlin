package com.acme.transport.emissions.fuelburn.group

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the fuel burn group calculation. */
interface FuelBurnGroupModifier {
    fun describe(): String
    fun adjust(item: FuelBurnGroup, value: Double): Double
}

data class TieredFuelBurnGroupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FuelBurnGroupModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FuelBurnGroup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedFuelBurnGroupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FuelBurnGroupModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FuelBurnGroup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentFuelBurnGroupAdjuster(var percent: Double) : FuelBurnGroupModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FuelBurnGroup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LaneFuelBurnGroupAdjuster(var factors: MutableMap<String, Double>) : FuelBurnGroupModifier {
    override fun describe(): String = "by lane"

    override fun adjust(item: FuelBurnGroup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapFuelBurnGroupAdjuster(var cap: Double) : FuelBurnGroupModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FuelBurnGroup, value: Double): Double = minOf(value, cap)
}

data class MinimumFuelBurnGroupAdjuster(var minimum: Double) : FuelBurnGroupModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FuelBurnGroup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the fuel burn group steps in order and keeps a trace of each. */
data class FuelBurnGroupEngine(
    var steps: MutableList<FuelBurnGroupModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: FuelBurnGroup): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FuelBurnGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultFuelBurnGroupEngine(flags: Registry): FuelBurnGroupEngine {
    return FuelBurnGroupEngine(
        steps = mutableListOf<FuelBurnGroupModifier>(
            TieredFuelBurnGroupAdjuster(
                mutableListOf<Double>(1417.0, 4179.0, 5599.0),
                mutableListOf<Double>(1.02, 0.83, 1.09),
            ),
            FlaggedFuelBurnGroupAdjuster(flags, "fuel-burn-group-rolling", 7.5),
            PercentFuelBurnGroupAdjuster(1.6),
            LaneFuelBurnGroupAdjuster(
                mutableMapOf<String, Double>("south" to 1.32, "retail" to 0.74, "north" to 0.92),
            ),
            CapFuelBurnGroupAdjuster(34474.0),
            MinimumFuelBurnGroupAdjuster(46.1),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 75535.0,
    )
}
