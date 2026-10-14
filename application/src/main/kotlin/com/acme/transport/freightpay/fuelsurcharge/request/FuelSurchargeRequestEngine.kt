package com.acme.transport.freightpay.fuelsurcharge.request

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the fuel surcharge request calculation. */
interface FuelSurchargeRequestModifier {
    fun describe(): String
    fun adjust(item: FuelSurchargeRequest, value: Double): Double
}

data class PercentFuelSurchargeRequestAdjuster(var percent: Double) : FuelSurchargeRequestModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FuelSurchargeRequest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumFuelSurchargeRequestAdjuster(var minimum: Double) : FuelSurchargeRequestModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FuelSurchargeRequest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapFuelSurchargeRequestAdjuster(var cap: Double) : FuelSurchargeRequestModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FuelSurchargeRequest, value: Double): Double = minOf(value, cap)
}

data class TieredFuelSurchargeRequestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FuelSurchargeRequestModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FuelSurchargeRequest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LabelFuelSurchargeRequestAdjuster(
    var factors: MutableMap<String, Double>,
) : FuelSurchargeRequestModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: FuelSurchargeRequest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the fuel surcharge request steps in order and keeps a trace of each. */
data class FuelSurchargeRequestEngine(
    var steps: MutableList<FuelSurchargeRequestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: FuelSurchargeRequest): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FuelSurchargeRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultFuelSurchargeRequestEngine(): FuelSurchargeRequestEngine {
    return FuelSurchargeRequestEngine(
        steps = mutableListOf<FuelSurchargeRequestModifier>(
            PercentFuelSurchargeRequestAdjuster(3.5),
            MinimumFuelSurchargeRequestAdjuster(33.8),
            CapFuelSurchargeRequestAdjuster(48199.0),
            TieredFuelSurchargeRequestAdjuster(
                mutableListOf<Double>(3167.0, 3353.0, 3768.0, 4245.0),
                mutableListOf<Double>(0.83, 1.08, 1.11, 0.96),
            ),
            LabelFuelSurchargeRequestAdjuster(
                mutableMapOf<String, Double>("west" to 1.31, "north" to 0.8, "south" to 1.15),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 30029.1,
    )
}
