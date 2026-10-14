package com.acme.transport.freightpay.fuelsurcharge.surchargeadjustment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the regional fuel surcharge adjustment calculation. */
interface RegionalFuelSurchargeAdjustmentFactor {
    fun describe(): String
    fun adjust(item: RegionalFuelSurchargeAdjustment, value: Double): Double
}

data class FlaggedRegionalFuelSurchargeAdjustmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalFuelSurchargeAdjustmentFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalFuelSurchargeAdjustment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingRegionalFuelSurchargeAdjustmentAdjuster(
    var step: Double,
) : RegionalFuelSurchargeAdjustmentFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalFuelSurchargeAdjustment, value: Double): Double = roundTo(value, step)
}

data class MinimumRegionalFuelSurchargeAdjustmentAdjuster(
    var minimum: Double,
) : RegionalFuelSurchargeAdjustmentFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalFuelSurchargeAdjustment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredRegionalFuelSurchargeAdjustmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalFuelSurchargeAdjustmentFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalFuelSurchargeAdjustment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RegionalFuelSurchargeAdjustmentDistanceKmOffsetAdjuster(
    var weight: Double,
) : RegionalFuelSurchargeAdjustmentFactor {
    override fun describe(): String = "plus distance km times $weight"

    override fun adjust(item: RegionalFuelSurchargeAdjustment, value: Double): Double {
        return value + item.distanceKm * weight
    }
}

/** Runs the regional fuel surcharge adjustment steps in order and keeps a trace of each. */
data class RegionalFuelSurchargeAdjustmentEngine(
    var steps: MutableList<RegionalFuelSurchargeAdjustmentFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RegionalFuelSurchargeAdjustment): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalFuelSurchargeAdjustmentFactor = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalFuelSurchargeAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRegionalFuelSurchargeAdjustmentEngine(flags: Registry): RegionalFuelSurchargeAdjustmentEngine {
    return RegionalFuelSurchargeAdjustmentEngine(
        steps = mutableListOf<RegionalFuelSurchargeAdjustmentFactor>(
            FlaggedRegionalFuelSurchargeAdjustmentAdjuster(
                flags,
                "regional-fuel-surcharge-adjustment-scheduled",
                6.9,
            ),
            RoundingRegionalFuelSurchargeAdjustmentAdjuster(0.25),
            MinimumRegionalFuelSurchargeAdjustmentAdjuster(0.4),
            TieredRegionalFuelSurchargeAdjustmentAdjuster(
                mutableListOf<Double>(507.0, 1713.0),
                mutableListOf<Double>(1.06, 0.98),
            ),
            RegionalFuelSurchargeAdjustmentDistanceKmOffsetAdjuster(0.64),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 48941.3,
    )
}
