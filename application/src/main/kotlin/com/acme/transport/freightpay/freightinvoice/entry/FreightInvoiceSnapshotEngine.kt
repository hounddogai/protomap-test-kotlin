package com.acme.transport.freightpay.freightinvoice.entry

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the freight invoice snapshot calculation. */
interface FreightInvoiceSnapshotModifier {
    fun describe(): String
    fun adjust(item: FreightInvoiceSnapshot, value: Double): Double
}

data class FlaggedFreightInvoiceSnapshotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FreightInvoiceSnapshotModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FreightInvoiceSnapshot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class FreightInvoiceSnapshotDiscountOffsetAdjuster(var weight: Double) : FreightInvoiceSnapshotModifier {
    override fun describe(): String = "plus discount times $weight"

    override fun adjust(item: FreightInvoiceSnapshot, value: Double): Double = value + item.discount * weight
}

data class CapFreightInvoiceSnapshotAdjuster(var cap: Double) : FreightInvoiceSnapshotModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FreightInvoiceSnapshot, value: Double): Double = minOf(value, cap)
}

/** Runs the freight invoice snapshot steps in order and keeps a trace of each. */
data class FreightInvoiceSnapshotEngine(
    var steps: MutableList<FreightInvoiceSnapshotModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: FreightInvoiceSnapshot): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FreightInvoiceSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultFreightInvoiceSnapshotEngine(flags: Registry): FreightInvoiceSnapshotEngine {
    return FreightInvoiceSnapshotEngine(
        steps = mutableListOf<FreightInvoiceSnapshotModifier>(
            FlaggedFreightInvoiceSnapshotAdjuster(flags, "freight-invoice-snapshot-global", -7.8),
            FreightInvoiceSnapshotDiscountOffsetAdjuster(1.09),
            CapFreightInvoiceSnapshotAdjuster(40679.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 9935.1,
    )
}
