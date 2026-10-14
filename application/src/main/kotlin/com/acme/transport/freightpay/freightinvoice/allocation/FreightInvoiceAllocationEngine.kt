package com.acme.transport.freightpay.freightinvoice.allocation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the freight invoice allocation calculation. */
interface FreightInvoiceAllocationModifier {
    fun describe(): String
    fun adjust(item: FreightInvoiceAllocation, value: Double): Double
}

data class CapFreightInvoiceAllocationAdjuster(var cap: Double) : FreightInvoiceAllocationModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FreightInvoiceAllocation, value: Double): Double = minOf(value, cap)
}

data class FlaggedFreightInvoiceAllocationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FreightInvoiceAllocationModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FreightInvoiceAllocation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingFreightInvoiceAllocationAdjuster(var step: Double) : FreightInvoiceAllocationModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FreightInvoiceAllocation, value: Double): Double = roundTo(value, step)
}

data class FreightInvoiceAllocationAttemptsOffsetAdjuster(
    var weight: Double,
) : FreightInvoiceAllocationModifier {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: FreightInvoiceAllocation, value: Double): Double {
        return value + item.attempts.toDouble() * weight
    }
}

data class DepotFreightInvoiceAllocationAdjuster(
    var factors: MutableMap<String, Double>,
) : FreightInvoiceAllocationModifier {
    override fun describe(): String = "by depot"

    override fun adjust(item: FreightInvoiceAllocation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumFreightInvoiceAllocationAdjuster(var minimum: Double) : FreightInvoiceAllocationModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FreightInvoiceAllocation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the freight invoice allocation steps in order and keeps a trace of each. */
data class FreightInvoiceAllocationEngine(
    var steps: MutableList<FreightInvoiceAllocationModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: FreightInvoiceAllocation): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FreightInvoiceAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultFreightInvoiceAllocationEngine(flags: Registry): FreightInvoiceAllocationEngine {
    return FreightInvoiceAllocationEngine(
        steps = mutableListOf<FreightInvoiceAllocationModifier>(
            CapFreightInvoiceAllocationAdjuster(43699.0),
            FlaggedFreightInvoiceAllocationAdjuster(flags, "freight-invoice-allocation-weighted", 7.1),
            RoundingFreightInvoiceAllocationAdjuster(5.0),
            FreightInvoiceAllocationAttemptsOffsetAdjuster(0.66),
            DepotFreightInvoiceAllocationAdjuster(
                mutableMapOf<String, Double>("west" to 1.19, "retail" to 0.93, "east" to 1.08),
            ),
            MinimumFreightInvoiceAllocationAdjuster(27.3),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 14859.6,
    )
}
