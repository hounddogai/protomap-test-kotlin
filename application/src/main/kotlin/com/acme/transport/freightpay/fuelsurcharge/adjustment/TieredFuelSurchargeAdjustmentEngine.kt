package com.acme.transport.freightpay.fuelsurcharge.adjustment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the tiered fuel surcharge adjustment calculation. */
interface TieredFuelSurchargeAdjustmentFactor {
    fun describe(): String
    fun adjust(item: TieredFuelSurchargeAdjustment, value: Double): Double
}

data class PercentTieredFuelSurchargeAdjustmentAdjuster(
    var percent: Double,
) : TieredFuelSurchargeAdjustmentFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredFuelSurchargeAdjustment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingTieredFuelSurchargeAdjustmentAdjuster(
    var step: Double,
) : TieredFuelSurchargeAdjustmentFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredFuelSurchargeAdjustment, value: Double): Double = roundTo(value, step)
}

data class TieredTieredFuelSurchargeAdjustmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredFuelSurchargeAdjustmentFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredFuelSurchargeAdjustment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedTieredFuelSurchargeAdjustmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredFuelSurchargeAdjustmentFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredFuelSurchargeAdjustment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapTieredFuelSurchargeAdjustmentAdjuster(var cap: Double) : TieredFuelSurchargeAdjustmentFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredFuelSurchargeAdjustment, value: Double): Double = minOf(value, cap)
}

/** Runs the tiered fuel surcharge adjustment steps in order and keeps a trace of each. */
data class TieredFuelSurchargeAdjustmentEngine(
    var steps: MutableList<TieredFuelSurchargeAdjustmentFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TieredFuelSurchargeAdjustment): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TieredFuelSurchargeAdjustmentFactor = steps[index]
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

    fun evaluateAll(items: MutableList<TieredFuelSurchargeAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTieredFuelSurchargeAdjustmentEngine(flags: Registry): TieredFuelSurchargeAdjustmentEngine {
    return TieredFuelSurchargeAdjustmentEngine(
        steps = mutableListOf<TieredFuelSurchargeAdjustmentFactor>(
            PercentTieredFuelSurchargeAdjustmentAdjuster(8.3),
            RoundingTieredFuelSurchargeAdjustmentAdjuster(0.1),
            TieredTieredFuelSurchargeAdjustmentAdjuster(
                mutableListOf<Double>(59.0, 5243.0, 6817.0, 8511.0),
                mutableListOf<Double>(1.16, 1.07, 0.92, 1.15),
            ),
            FlaggedTieredFuelSurchargeAdjustmentAdjuster(flags, "tiered-fuel-surcharge-adjustment-manual", -8.7),
            CapTieredFuelSurchargeAdjustmentAdjuster(19738.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 34134.2,
    )
}
