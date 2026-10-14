package com.acme.transport.freightpay.freightinvoice.invoiceadjustment

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the rolling freight invoice request calculation. */
interface RollingFreightInvoiceRequestModifier {
    fun describe(): String
    fun adjust(item: RollingFreightInvoiceRequest, value: Double): Double
}

data class CapRollingFreightInvoiceRequestAdjuster(var cap: Double) : RollingFreightInvoiceRequestModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingFreightInvoiceRequest, value: Double): Double = minOf(value, cap)
}

data class MinimumRollingFreightInvoiceRequestAdjuster(
    var minimum: Double,
) : RollingFreightInvoiceRequestModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingFreightInvoiceRequest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredRollingFreightInvoiceRequestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingFreightInvoiceRequestModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingFreightInvoiceRequest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RollingFreightInvoiceRequestMarginOffsetAdjuster(
    var weight: Double,
) : RollingFreightInvoiceRequestModifier {
    override fun describe(): String = "plus margin times $weight"

    override fun adjust(item: RollingFreightInvoiceRequest, value: Double): Double = value + item.margin * weight
}

/** Runs the rolling freight invoice request steps in order and keeps a trace of each. */
data class RollingFreightInvoiceRequestEngine(
    var steps: MutableList<RollingFreightInvoiceRequestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RollingFreightInvoiceRequest): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RollingFreightInvoiceRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRollingFreightInvoiceRequestEngine(): RollingFreightInvoiceRequestEngine {
    return RollingFreightInvoiceRequestEngine(
        steps = mutableListOf<RollingFreightInvoiceRequestModifier>(
            CapRollingFreightInvoiceRequestAdjuster(44390.0),
            MinimumRollingFreightInvoiceRequestAdjuster(28.4),
            TieredRollingFreightInvoiceRequestAdjuster(
                mutableListOf<Double>(43.0, 1915.0),
                mutableListOf<Double>(1.13, 0.92),
            ),
            RollingFreightInvoiceRequestMarginOffsetAdjuster(1.33),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 9432.6,
    )
}
