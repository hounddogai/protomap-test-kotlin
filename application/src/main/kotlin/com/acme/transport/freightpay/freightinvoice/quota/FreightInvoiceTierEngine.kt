package com.acme.transport.freightpay.freightinvoice.quota

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the freight invoice tier calculation. */
interface FreightInvoiceTierAdjuster {
    fun describe(): String
    fun adjust(item: FreightInvoiceTier, value: Double): Double
}

data class RoundingFreightInvoiceTierAdjuster(var step: Double) : FreightInvoiceTierAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FreightInvoiceTier, value: Double): Double = roundTo(value, step)
}

data class TieredFreightInvoiceTierAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FreightInvoiceTierAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FreightInvoiceTier, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapFreightInvoiceTierAdjuster(var cap: Double) : FreightInvoiceTierAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FreightInvoiceTier, value: Double): Double = minOf(value, cap)
}

data class NameFreightInvoiceTierAdjuster(
    var factors: MutableMap<String, Double>,
) : FreightInvoiceTierAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: FreightInvoiceTier, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumFreightInvoiceTierAdjuster(var minimum: Double) : FreightInvoiceTierAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FreightInvoiceTier, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedFreightInvoiceTierAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FreightInvoiceTierAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FreightInvoiceTier, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the freight invoice tier steps in order and keeps a trace of each. */
data class FreightInvoiceTierEngine(
    var steps: MutableList<FreightInvoiceTierAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: FreightInvoiceTier): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FreightInvoiceTierAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<FreightInvoiceTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultFreightInvoiceTierEngine(flags: Registry): FreightInvoiceTierEngine {
    return FreightInvoiceTierEngine(
        steps = mutableListOf<FreightInvoiceTierAdjuster>(
            RoundingFreightInvoiceTierAdjuster(0.05),
            TieredFreightInvoiceTierAdjuster(
                mutableListOf<Double>(4999.0, 6090.0, 9089.0),
                mutableListOf<Double>(1.14, 1.05, 1.18),
            ),
            CapFreightInvoiceTierAdjuster(29924.0),
            NameFreightInvoiceTierAdjuster(mutableMapOf<String, Double>("east" to 0.97, "south" to 0.74)),
            MinimumFreightInvoiceTierAdjuster(8.6),
            FlaggedFreightInvoiceTierAdjuster(flags, "freight-invoice-tier-lenient", 2.2),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 3085.0,
    )
}
