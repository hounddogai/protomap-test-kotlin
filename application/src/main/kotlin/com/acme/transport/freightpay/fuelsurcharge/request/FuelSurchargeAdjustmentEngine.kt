package com.acme.transport.freightpay.fuelsurcharge.request

import com.acme.transport.kit.flags.Registry

/** One step of the fuel surcharge adjustment calculation. */
interface FuelSurchargeAdjustmentModifier {
    fun describe(): String
    fun adjust(item: FuelSurchargeAdjustment, value: Double): Double
}

data class NameFuelSurchargeAdjustmentAdjuster(
    var factors: MutableMap<String, Double>,
) : FuelSurchargeAdjustmentModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: FuelSurchargeAdjustment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentFuelSurchargeAdjustmentAdjuster(var percent: Double) : FuelSurchargeAdjustmentModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FuelSurchargeAdjustment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapFuelSurchargeAdjustmentAdjuster(var cap: Double) : FuelSurchargeAdjustmentModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FuelSurchargeAdjustment, value: Double): Double = minOf(value, cap)
}

data class FlaggedFuelSurchargeAdjustmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FuelSurchargeAdjustmentModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FuelSurchargeAdjustment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the fuel surcharge adjustment steps in order and keeps a trace of each. */
data class FuelSurchargeAdjustmentEngine(
    var steps: MutableList<FuelSurchargeAdjustmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: FuelSurchargeAdjustment): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FuelSurchargeAdjustmentModifier = steps[index]
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

    fun evaluateAll(items: MutableList<FuelSurchargeAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultFuelSurchargeAdjustmentEngine(flags: Registry): FuelSurchargeAdjustmentEngine {
    return FuelSurchargeAdjustmentEngine(
        steps = mutableListOf<FuelSurchargeAdjustmentModifier>(
            NameFuelSurchargeAdjustmentAdjuster(
                mutableMapOf<String, Double>("online" to 1.32, "central" to 1.17, "north" to 1.12),
            ),
            PercentFuelSurchargeAdjustmentAdjuster(14.6),
            CapFuelSurchargeAdjustmentAdjuster(42146.0),
            FlaggedFuelSurchargeAdjustmentAdjuster(flags, "fuel-surcharge-adjustment-batched", -2.2),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 8712.0,
    )
}
