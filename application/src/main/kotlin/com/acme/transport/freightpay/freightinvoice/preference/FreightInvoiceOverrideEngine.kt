package com.acme.transport.freightpay.freightinvoice.preference

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the freight invoice override calculation. */
interface FreightInvoiceOverrideAdjuster {
    fun describe(): String
    fun adjust(item: FreightInvoiceOverride, value: Double): Double
}

data class CapFreightInvoiceOverrideAdjuster(var cap: Double) : FreightInvoiceOverrideAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FreightInvoiceOverride, value: Double): Double = minOf(value, cap)
}

data class RoundingFreightInvoiceOverrideAdjuster(var step: Double) : FreightInvoiceOverrideAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FreightInvoiceOverride, value: Double): Double = roundTo(value, step)
}

data class FlaggedFreightInvoiceOverrideAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FreightInvoiceOverrideAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FreightInvoiceOverride, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredFreightInvoiceOverrideAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FreightInvoiceOverrideAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FreightInvoiceOverride, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CountryFreightInvoiceOverrideAdjuster(
    var factors: MutableMap<String, Double>,
) : FreightInvoiceOverrideAdjuster {
    override fun describe(): String = "by country"

    override fun adjust(item: FreightInvoiceOverride, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumFreightInvoiceOverrideAdjuster(var minimum: Double) : FreightInvoiceOverrideAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FreightInvoiceOverride, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the freight invoice override steps in order and keeps a trace of each. */
data class FreightInvoiceOverrideEngine(
    var steps: MutableList<FreightInvoiceOverrideAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: FreightInvoiceOverride): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FreightInvoiceOverrideAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<FreightInvoiceOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultFreightInvoiceOverrideEngine(flags: Registry): FreightInvoiceOverrideEngine {
    return FreightInvoiceOverrideEngine(
        steps = mutableListOf<FreightInvoiceOverrideAdjuster>(
            CapFreightInvoiceOverrideAdjuster(33813.0),
            RoundingFreightInvoiceOverrideAdjuster(1.0),
            FlaggedFreightInvoiceOverrideAdjuster(flags, "freight-invoice-override-rolling", -2.4),
            TieredFreightInvoiceOverrideAdjuster(
                mutableListOf<Double>(5394.0, 5964.0, 9352.0, 9898.0),
                mutableListOf<Double>(1.08, 0.96, 1.11, 1.02),
            ),
            CountryFreightInvoiceOverrideAdjuster(mutableMapOf<String, Double>("south" to 0.92)),
            MinimumFreightInvoiceOverrideAdjuster(30.5),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 46502.7,
    )
}
