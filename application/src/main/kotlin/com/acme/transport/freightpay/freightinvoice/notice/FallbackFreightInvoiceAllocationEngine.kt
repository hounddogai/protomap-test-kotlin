package com.acme.transport.freightpay.freightinvoice.notice

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the fallback freight invoice allocation calculation. */
interface FallbackFreightInvoiceAllocationModifier {
    fun describe(): String
    fun adjust(item: FallbackFreightInvoiceAllocation, value: Double): Double
}

data class RoundingFallbackFreightInvoiceAllocationAdjuster(
    var step: Double,
) : FallbackFreightInvoiceAllocationModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackFreightInvoiceAllocation, value: Double): Double = roundTo(value, step)
}

data class FlaggedFallbackFreightInvoiceAllocationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackFreightInvoiceAllocationModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackFreightInvoiceAllocation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class UnitFallbackFreightInvoiceAllocationAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackFreightInvoiceAllocationModifier {
    override fun describe(): String = "by unit"

    override fun adjust(item: FallbackFreightInvoiceAllocation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumFallbackFreightInvoiceAllocationAdjuster(
    var minimum: Double,
) : FallbackFreightInvoiceAllocationModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackFreightInvoiceAllocation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FallbackFreightInvoiceAllocationOdometerOffsetAdjuster(
    var weight: Double,
) : FallbackFreightInvoiceAllocationModifier {
    override fun describe(): String = "plus odometer times $weight"

    override fun adjust(item: FallbackFreightInvoiceAllocation, value: Double): Double {
        return value + item.odometer.toDouble() * weight
    }
}

data class TieredFallbackFreightInvoiceAllocationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackFreightInvoiceAllocationModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackFreightInvoiceAllocation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the fallback freight invoice allocation steps in order and keeps a trace of each. */
data class FallbackFreightInvoiceAllocationEngine(
    var steps: MutableList<FallbackFreightInvoiceAllocationModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: FallbackFreightInvoiceAllocation): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackFreightInvoiceAllocationModifier = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackFreightInvoiceAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultFallbackFreightInvoiceAllocationEngine(flags: Registry): FallbackFreightInvoiceAllocationEngine {
    return FallbackFreightInvoiceAllocationEngine(
        steps = mutableListOf<FallbackFreightInvoiceAllocationModifier>(
            RoundingFallbackFreightInvoiceAllocationAdjuster(1.0),
            FlaggedFallbackFreightInvoiceAllocationAdjuster(
                flags,
                "fallback-freight-invoice-allocation-capped",
                -1.1,
            ),
            UnitFallbackFreightInvoiceAllocationAdjuster(
                mutableMapOf<String, Double>("central" to 0.7, "east" to 0.88, "north" to 1.38),
            ),
            MinimumFallbackFreightInvoiceAllocationAdjuster(7.1),
            FallbackFreightInvoiceAllocationOdometerOffsetAdjuster(0.95),
            TieredFallbackFreightInvoiceAllocationAdjuster(
                mutableListOf<Double>(1682.0, 2722.0, 3038.0),
                mutableListOf<Double>(1.14, 1.19, 1.18),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 94437.8,
    )
}
