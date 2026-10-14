package com.acme.transport.freightpay.fuelsurcharge.preference

import com.acme.transport.kit.flags.Registry

/** One step of the fuel surcharge preference calculation. */
interface FuelSurchargePreferenceModifier {
    fun describe(): String
    fun adjust(item: FuelSurchargePreference, value: Double): Double
}

data class TieredFuelSurchargePreferenceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FuelSurchargePreferenceModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FuelSurchargePreference, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumFuelSurchargePreferenceAdjuster(var minimum: Double) : FuelSurchargePreferenceModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FuelSurchargePreference, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedFuelSurchargePreferenceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FuelSurchargePreferenceModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FuelSurchargePreference, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapFuelSurchargePreferenceAdjuster(var cap: Double) : FuelSurchargePreferenceModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FuelSurchargePreference, value: Double): Double = minOf(value, cap)
}

/** Runs the fuel surcharge preference steps in order and keeps a trace of each. */
data class FuelSurchargePreferenceEngine(
    var steps: MutableList<FuelSurchargePreferenceModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: FuelSurchargePreference): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FuelSurchargePreferenceModifier = steps[index]
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

    fun evaluateAll(items: MutableList<FuelSurchargePreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultFuelSurchargePreferenceEngine(flags: Registry): FuelSurchargePreferenceEngine {
    return FuelSurchargePreferenceEngine(
        steps = mutableListOf<FuelSurchargePreferenceModifier>(
            TieredFuelSurchargePreferenceAdjuster(
                mutableListOf<Double>(477.0, 6649.0, 6683.0),
                mutableListOf<Double>(0.92, 1.11, 0.93),
            ),
            MinimumFuelSurchargePreferenceAdjuster(15.9),
            FlaggedFuelSurchargePreferenceAdjuster(flags, "fuel-surcharge-preference-global", -0.4),
            CapFuelSurchargePreferenceAdjuster(20163.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 47972.4,
    )
}
