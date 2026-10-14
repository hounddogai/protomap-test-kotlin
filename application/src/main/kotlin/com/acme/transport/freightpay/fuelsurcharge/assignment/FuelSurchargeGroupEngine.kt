package com.acme.transport.freightpay.fuelsurcharge.assignment

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the fuel surcharge group calculation. */
interface FuelSurchargeGroupStep {
    fun describe(): String
    fun adjust(item: FuelSurchargeGroup, value: Double): Double
}

data class PercentFuelSurchargeGroupAdjuster(var percent: Double) : FuelSurchargeGroupStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FuelSurchargeGroup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapFuelSurchargeGroupAdjuster(var cap: Double) : FuelSurchargeGroupStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FuelSurchargeGroup, value: Double): Double = minOf(value, cap)
}

data class TieredFuelSurchargeGroupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FuelSurchargeGroupStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FuelSurchargeGroup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumFuelSurchargeGroupAdjuster(var minimum: Double) : FuelSurchargeGroupStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FuelSurchargeGroup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the fuel surcharge group steps in order and keeps a trace of each. */
data class FuelSurchargeGroupEngine(
    var steps: MutableList<FuelSurchargeGroupStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: FuelSurchargeGroup): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FuelSurchargeGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultFuelSurchargeGroupEngine(): FuelSurchargeGroupEngine {
    return FuelSurchargeGroupEngine(
        steps = mutableListOf<FuelSurchargeGroupStep>(
            PercentFuelSurchargeGroupAdjuster(-12.4),
            CapFuelSurchargeGroupAdjuster(19214.0),
            TieredFuelSurchargeGroupAdjuster(
                mutableListOf<Double>(444.0, 3778.0, 7413.0),
                mutableListOf<Double>(0.83, 0.9, 0.87),
            ),
            MinimumFuelSurchargeGroupAdjuster(26.2),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 2042.5,
    )
}
