package com.acme.transport.freightpay.fuelsurcharge.template

import com.acme.transport.kit.flags.Registry

/** One step of the fuel surcharge template calculation. */
interface FuelSurchargeTemplateAdjuster {
    fun describe(): String
    fun adjust(item: FuelSurchargeTemplate, value: Double): Double
}

data class FlaggedFuelSurchargeTemplateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FuelSurchargeTemplateAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FuelSurchargeTemplate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentFuelSurchargeTemplateAdjuster(var percent: Double) : FuelSurchargeTemplateAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FuelSurchargeTemplate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FuelSurchargeTemplateCapacityOffsetAdjuster(var weight: Double) : FuelSurchargeTemplateAdjuster {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: FuelSurchargeTemplate, value: Double): Double {
        return value + item.capacity.toDouble() * weight
    }
}

data class CapFuelSurchargeTemplateAdjuster(var cap: Double) : FuelSurchargeTemplateAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FuelSurchargeTemplate, value: Double): Double = minOf(value, cap)
}

/** Runs the fuel surcharge template steps in order and keeps a trace of each. */
data class FuelSurchargeTemplateEngine(
    var steps: MutableList<FuelSurchargeTemplateAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: FuelSurchargeTemplate): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FuelSurchargeTemplateAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<FuelSurchargeTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultFuelSurchargeTemplateEngine(flags: Registry): FuelSurchargeTemplateEngine {
    return FuelSurchargeTemplateEngine(
        steps = mutableListOf<FuelSurchargeTemplateAdjuster>(
            FlaggedFuelSurchargeTemplateAdjuster(flags, "fuel-surcharge-template-seasonal", 5.9),
            PercentFuelSurchargeTemplateAdjuster(-4.3),
            FuelSurchargeTemplateCapacityOffsetAdjuster(1.12),
            CapFuelSurchargeTemplateAdjuster(33991.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 17259.0,
    )
}
