package com.acme.transport.freightpay.freightinvoice.invoiceentry

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the lenient freight invoice entry calculation. */
interface LenientFreightInvoiceEntryStep {
    fun describe(): String
    fun adjust(item: LenientFreightInvoiceEntry, value: Double): Double
}

data class TieredLenientFreightInvoiceEntryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientFreightInvoiceEntryStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientFreightInvoiceEntry, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapLenientFreightInvoiceEntryAdjuster(var cap: Double) : LenientFreightInvoiceEntryStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientFreightInvoiceEntry, value: Double): Double = minOf(value, cap)
}

data class RoundingLenientFreightInvoiceEntryAdjuster(var step: Double) : LenientFreightInvoiceEntryStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientFreightInvoiceEntry, value: Double): Double = roundTo(value, step)
}

/** Runs the lenient freight invoice entry steps in order and keeps a trace of each. */
data class LenientFreightInvoiceEntryEngine(
    var steps: MutableList<LenientFreightInvoiceEntryStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LenientFreightInvoiceEntry): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LenientFreightInvoiceEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLenientFreightInvoiceEntryEngine(): LenientFreightInvoiceEntryEngine {
    return LenientFreightInvoiceEntryEngine(
        steps = mutableListOf<LenientFreightInvoiceEntryStep>(
            TieredLenientFreightInvoiceEntryAdjuster(
                mutableListOf<Double>(1396.0, 7055.0),
                mutableListOf<Double>(0.85, 1.14),
            ),
            CapLenientFreightInvoiceEntryAdjuster(40245.0),
            RoundingLenientFreightInvoiceEntryAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 16536.8,
    )
}
