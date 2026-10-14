package com.acme.transport.freightpay.fuelsurcharge.surchargepreference

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the fuel surcharge claim calculation. */
interface FuelSurchargeClaimAdjuster {
    fun describe(): String
    fun adjust(item: FuelSurchargeClaim, value: Double): Double
}

data class PercentFuelSurchargeClaimAdjuster(var percent: Double) : FuelSurchargeClaimAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FuelSurchargeClaim, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredFuelSurchargeClaimAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FuelSurchargeClaimAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FuelSurchargeClaim, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedFuelSurchargeClaimAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FuelSurchargeClaimAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FuelSurchargeClaim, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the fuel surcharge claim steps in order and keeps a trace of each. */
data class FuelSurchargeClaimEngine(
    var steps: MutableList<FuelSurchargeClaimAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: FuelSurchargeClaim): Double {
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

    fun evaluateAll(items: MutableList<FuelSurchargeClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultFuelSurchargeClaimEngine(flags: Registry): FuelSurchargeClaimEngine {
    return FuelSurchargeClaimEngine(
        steps = mutableListOf<FuelSurchargeClaimAdjuster>(
            PercentFuelSurchargeClaimAdjuster(17.2),
            TieredFuelSurchargeClaimAdjuster(
                mutableListOf<Double>(4390.0, 4471.0, 4705.0, 6704.0),
                mutableListOf<Double>(0.85, 0.95, 0.91, 1.02),
            ),
            FlaggedFuelSurchargeClaimAdjuster(flags, "fuel-surcharge-claim-manual", 0.8),
        ),
        trace = mutableListOf<String>(),
        floor = 3.1,
        ceiling = 59085.0,
    )
}
