package com.acme.transport.freightpay.freightinvoice.invoicemanifest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the weighted freight invoice policy calculation. */
interface WeightedFreightInvoicePolicyStep {
    fun describe(): String
    fun adjust(item: WeightedFreightInvoicePolicy, value: Double): Double
}

data class CapWeightedFreightInvoicePolicyAdjuster(var cap: Double) : WeightedFreightInvoicePolicyStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedFreightInvoicePolicy, value: Double): Double = minOf(value, cap)
}

data class TieredWeightedFreightInvoicePolicyAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedFreightInvoicePolicyStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedFreightInvoicePolicy, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class WeightedFreightInvoicePolicyOdometerOffsetAdjuster(
    var weight: Double,
) : WeightedFreightInvoicePolicyStep {
    override fun describe(): String = "plus odometer times $weight"

    override fun adjust(item: WeightedFreightInvoicePolicy, value: Double): Double {
        return value + item.odometer.toDouble() * weight
    }
}

data class MinimumWeightedFreightInvoicePolicyAdjuster(var minimum: Double) : WeightedFreightInvoicePolicyStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedFreightInvoicePolicy, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentWeightedFreightInvoicePolicyAdjuster(var percent: Double) : WeightedFreightInvoicePolicyStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedFreightInvoicePolicy, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedWeightedFreightInvoicePolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedFreightInvoicePolicyStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedFreightInvoicePolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the weighted freight invoice policy steps in order and keeps a trace of each. */
data class WeightedFreightInvoicePolicyEngine(
    var steps: MutableList<WeightedFreightInvoicePolicyStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: WeightedFreightInvoicePolicy): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<WeightedFreightInvoicePolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultWeightedFreightInvoicePolicyEngine(flags: Registry): WeightedFreightInvoicePolicyEngine {
    return WeightedFreightInvoicePolicyEngine(
        steps = mutableListOf<WeightedFreightInvoicePolicyStep>(
            CapWeightedFreightInvoicePolicyAdjuster(13391.0),
            TieredWeightedFreightInvoicePolicyAdjuster(
                mutableListOf<Double>(63.0, 1227.0, 2386.0),
                mutableListOf<Double>(0.99, 0.88, 0.87),
            ),
            WeightedFreightInvoicePolicyOdometerOffsetAdjuster(1.25),
            MinimumWeightedFreightInvoicePolicyAdjuster(43.1),
            PercentWeightedFreightInvoicePolicyAdjuster(-14.0),
            FlaggedWeightedFreightInvoicePolicyAdjuster(flags, "weighted-freight-invoice-policy-capped", -3.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 30686.6,
    )
}
