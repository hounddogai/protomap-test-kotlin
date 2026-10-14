package com.acme.transport.freightpay.fuelsurcharge.slot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the fuel surcharge slot calculation. */
interface FuelSurchargeSlotModifier {
    fun describe(): String
    fun adjust(item: FuelSurchargeSlot, value: Double): Double
}

data class FuelSurchargeSlotPalletsOffsetAdjuster(var weight: Double) : FuelSurchargeSlotModifier {
    override fun describe(): String = "plus pallets times $weight"

    override fun adjust(item: FuelSurchargeSlot, value: Double): Double = value + item.pallets.toDouble() * weight
}

data class PercentFuelSurchargeSlotAdjuster(var percent: Double) : FuelSurchargeSlotModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FuelSurchargeSlot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingFuelSurchargeSlotAdjuster(var step: Double) : FuelSurchargeSlotModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FuelSurchargeSlot, value: Double): Double = roundTo(value, step)
}

data class FlaggedFuelSurchargeSlotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FuelSurchargeSlotModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FuelSurchargeSlot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredFuelSurchargeSlotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FuelSurchargeSlotModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FuelSurchargeSlot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class TitleFuelSurchargeSlotAdjuster(var factors: MutableMap<String, Double>) : FuelSurchargeSlotModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: FuelSurchargeSlot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the fuel surcharge slot steps in order and keeps a trace of each. */
data class FuelSurchargeSlotEngine(
    var steps: MutableList<FuelSurchargeSlotModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: FuelSurchargeSlot): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FuelSurchargeSlotModifier = steps[index]
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

    fun evaluateAll(items: MutableList<FuelSurchargeSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultFuelSurchargeSlotEngine(flags: Registry): FuelSurchargeSlotEngine {
    return FuelSurchargeSlotEngine(
        steps = mutableListOf<FuelSurchargeSlotModifier>(
            FuelSurchargeSlotPalletsOffsetAdjuster(1.91),
            PercentFuelSurchargeSlotAdjuster(-23.3),
            RoundingFuelSurchargeSlotAdjuster(0.25),
            FlaggedFuelSurchargeSlotAdjuster(flags, "fuel-surcharge-slot-seasonal", 2.4),
            TieredFuelSurchargeSlotAdjuster(
                mutableListOf<Double>(967.0, 1354.0, 2160.0, 7118.0),
                mutableListOf<Double>(0.82, 0.81, 1.11, 0.95),
            ),
            TitleFuelSurchargeSlotAdjuster(mutableMapOf<String, Double>("online" to 1.03, "south" to 1.09)),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 76419.5,
    )
}
