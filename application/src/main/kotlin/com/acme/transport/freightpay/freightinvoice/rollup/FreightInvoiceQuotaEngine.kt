package com.acme.transport.freightpay.freightinvoice.rollup

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the freight invoice quota calculation. */
interface FreightInvoiceQuotaStep {
    fun describe(): String
    fun adjust(item: FreightInvoiceQuota, value: Double): Double
}

data class CapFreightInvoiceQuotaAdjuster(var cap: Double) : FreightInvoiceQuotaStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FreightInvoiceQuota, value: Double): Double = minOf(value, cap)
}

data class FreightInvoiceQuotaCreatedAtOffsetAdjuster(var weight: Double) : FreightInvoiceQuotaStep {
    override fun describe(): String = "plus created at times $weight"

    override fun adjust(item: FreightInvoiceQuota, value: Double): Double = value + item.createdAt.toDouble() * weight
}

data class RoundingFreightInvoiceQuotaAdjuster(var step: Double) : FreightInvoiceQuotaStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FreightInvoiceQuota, value: Double): Double = roundTo(value, step)
}

/** Runs the freight invoice quota steps in order and keeps a trace of each. */
data class FreightInvoiceQuotaEngine(
    var steps: MutableList<FreightInvoiceQuotaStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: FreightInvoiceQuota): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FreightInvoiceQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultFreightInvoiceQuotaEngine(): FreightInvoiceQuotaEngine {
    return FreightInvoiceQuotaEngine(
        steps = mutableListOf<FreightInvoiceQuotaStep>(
            CapFreightInvoiceQuotaAdjuster(38005.0),
            FreightInvoiceQuotaCreatedAtOffsetAdjuster(1.33),
            RoundingFreightInvoiceQuotaAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 32384.2,
    )
}
