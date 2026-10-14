package com.acme.transport.freightpay.freightinvoice.invoicequota

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rolling freight invoice quota calculation. */
interface RollingFreightInvoiceQuotaModifier {
    fun describe(): String
    fun adjust(item: RollingFreightInvoiceQuota, value: Double): Double
}

data class PercentRollingFreightInvoiceQuotaAdjuster(var percent: Double) : RollingFreightInvoiceQuotaModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingFreightInvoiceQuota, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumRollingFreightInvoiceQuotaAdjuster(var minimum: Double) : RollingFreightInvoiceQuotaModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingFreightInvoiceQuota, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RollingFreightInvoiceQuotaWeightOffsetAdjuster(
    var weight: Double,
) : RollingFreightInvoiceQuotaModifier {
    override fun describe(): String = "plus weight times $weight"

    override fun adjust(item: RollingFreightInvoiceQuota, value: Double): Double = value + item.weight * weight
}

data class RoundingRollingFreightInvoiceQuotaAdjuster(var step: Double) : RollingFreightInvoiceQuotaModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingFreightInvoiceQuota, value: Double): Double = roundTo(value, step)
}

/** Runs the rolling freight invoice quota steps in order and keeps a trace of each. */
data class RollingFreightInvoiceQuotaEngine(
    var steps: MutableList<RollingFreightInvoiceQuotaModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RollingFreightInvoiceQuota): Double {
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

    fun evaluateAll(items: MutableList<RollingFreightInvoiceQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRollingFreightInvoiceQuotaEngine(): RollingFreightInvoiceQuotaEngine {
    return RollingFreightInvoiceQuotaEngine(
        steps = mutableListOf<RollingFreightInvoiceQuotaModifier>(
            PercentRollingFreightInvoiceQuotaAdjuster(-19.1),
            MinimumRollingFreightInvoiceQuotaAdjuster(35.1),
            RollingFreightInvoiceQuotaWeightOffsetAdjuster(1.03),
            RoundingRollingFreightInvoiceQuotaAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 28972.8,
    )
}
