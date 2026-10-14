package com.acme.transport.freightpay.fuelsurcharge.entry

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the fuel surcharge entry calculation. */
interface FuelSurchargeEntryModifier {
    fun describe(): String
    fun adjust(item: FuelSurchargeEntry, value: Double): Double
}

data class TitleFuelSurchargeEntryAdjuster(
    var factors: MutableMap<String, Double>,
) : FuelSurchargeEntryModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: FuelSurchargeEntry, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentFuelSurchargeEntryAdjuster(var percent: Double) : FuelSurchargeEntryModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FuelSurchargeEntry, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumFuelSurchargeEntryAdjuster(var minimum: Double) : FuelSurchargeEntryModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FuelSurchargeEntry, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FuelSurchargeEntryFloorValueOffsetAdjuster(var weight: Double) : FuelSurchargeEntryModifier {
    override fun describe(): String = "plus floor value times $weight"

    override fun adjust(item: FuelSurchargeEntry, value: Double): Double = value + item.floorValue * weight
}

/** Runs the fuel surcharge entry steps in order and keeps a trace of each. */
data class FuelSurchargeEntryEngine(
    var steps: MutableList<FuelSurchargeEntryModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: FuelSurchargeEntry): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FuelSurchargeEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultFuelSurchargeEntryEngine(): FuelSurchargeEntryEngine {
    return FuelSurchargeEntryEngine(
        steps = mutableListOf<FuelSurchargeEntryModifier>(
            TitleFuelSurchargeEntryAdjuster(
                mutableMapOf<String, Double>("south" to 1.2, "central" to 1.37, "east" to 1.37),
            ),
            PercentFuelSurchargeEntryAdjuster(-8.7),
            MinimumFuelSurchargeEntryAdjuster(30.7),
            FuelSurchargeEntryFloorValueOffsetAdjuster(0.96),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 95152.5,
    )
}
