package com.acme.transport.emissions.fuelburn.request

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the fuel burn slot calculation. */
interface FuelBurnSlotFactor {
    fun describe(): String
    fun adjust(item: FuelBurnSlot, value: Double): Double
}

data class MinimumFuelBurnSlotAdjuster(var minimum: Double) : FuelBurnSlotFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FuelBurnSlot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentFuelBurnSlotAdjuster(var percent: Double) : FuelBurnSlotFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FuelBurnSlot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapFuelBurnSlotAdjuster(var cap: Double) : FuelBurnSlotFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FuelBurnSlot, value: Double): Double = minOf(value, cap)
}

/** Runs the fuel burn slot steps in order and keeps a trace of each. */
data class FuelBurnSlotEngine(
    var steps: MutableList<FuelBurnSlotFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: FuelBurnSlot): Double {
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

    fun evaluateAll(items: MutableList<FuelBurnSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultFuelBurnSlotEngine(): FuelBurnSlotEngine {
    return FuelBurnSlotEngine(
        steps = mutableListOf<FuelBurnSlotFactor>(
            MinimumFuelBurnSlotAdjuster(24.6),
            PercentFuelBurnSlotAdjuster(7.4),
            CapFuelBurnSlotAdjuster(5879.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 7468.6,
    )
}
