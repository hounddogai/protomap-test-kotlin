package com.acme.transport.freightpay.fuelsurcharge.surchargewindow

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the fuel surcharge tier calculation. */
interface FuelSurchargeTierModifier {
    fun describe(): String
    fun adjust(item: FuelSurchargeTier, value: Double): Double
}

data class FuelSurchargeTierQuantityOffsetAdjuster(var weight: Double) : FuelSurchargeTierModifier {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: FuelSurchargeTier, value: Double): Double = value + item.quantity.toDouble() * weight
}

data class RoundingFuelSurchargeTierAdjuster(var step: Double) : FuelSurchargeTierModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FuelSurchargeTier, value: Double): Double = roundTo(value, step)
}

data class TieredFuelSurchargeTierAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FuelSurchargeTierModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FuelSurchargeTier, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentFuelSurchargeTierAdjuster(var percent: Double) : FuelSurchargeTierModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FuelSurchargeTier, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedFuelSurchargeTierAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FuelSurchargeTierModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FuelSurchargeTier, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumFuelSurchargeTierAdjuster(var minimum: Double) : FuelSurchargeTierModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FuelSurchargeTier, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the fuel surcharge tier steps in order and keeps a trace of each. */
data class FuelSurchargeTierEngine(
    var steps: MutableList<FuelSurchargeTierModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: FuelSurchargeTier): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FuelSurchargeTierModifier = steps[index]
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

    fun evaluateAll(items: MutableList<FuelSurchargeTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultFuelSurchargeTierEngine(flags: Registry): FuelSurchargeTierEngine {
    return FuelSurchargeTierEngine(
        steps = mutableListOf<FuelSurchargeTierModifier>(
            FuelSurchargeTierQuantityOffsetAdjuster(1.03),
            RoundingFuelSurchargeTierAdjuster(1.0),
            TieredFuelSurchargeTierAdjuster(
                mutableListOf<Double>(5560.0, 8628.0),
                mutableListOf<Double>(1.16, 0.91),
            ),
            PercentFuelSurchargeTierAdjuster(-21.6),
            FlaggedFuelSurchargeTierAdjuster(flags, "fuel-surcharge-tier-manual", -3.5),
            MinimumFuelSurchargeTierAdjuster(22.8),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 74434.7,
    )
}
