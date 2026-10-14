package com.acme.transport.freightpay.freightinvoice.rollup

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the freight invoice rollup calculation. */
interface FreightInvoiceRollupAdjuster {
    fun describe(): String
    fun adjust(item: FreightInvoiceRollup, value: Double): Double
}

data class PercentFreightInvoiceRollupAdjuster(var percent: Double) : FreightInvoiceRollupAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FreightInvoiceRollup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingFreightInvoiceRollupAdjuster(var step: Double) : FreightInvoiceRollupAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FreightInvoiceRollup, value: Double): Double = roundTo(value, step)
}

data class MinimumFreightInvoiceRollupAdjuster(var minimum: Double) : FreightInvoiceRollupAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FreightInvoiceRollup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FreightInvoiceRollupRatioOffsetAdjuster(var weight: Double) : FreightInvoiceRollupAdjuster {
    override fun describe(): String = "plus ratio times $weight"

    override fun adjust(item: FreightInvoiceRollup, value: Double): Double = value + item.ratio * weight
}

/** Runs the freight invoice rollup steps in order and keeps a trace of each. */
data class FreightInvoiceRollupEngine(
    var steps: MutableList<FreightInvoiceRollupAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: FreightInvoiceRollup): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FreightInvoiceRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultFreightInvoiceRollupEngine(): FreightInvoiceRollupEngine {
    return FreightInvoiceRollupEngine(
        steps = mutableListOf<FreightInvoiceRollupAdjuster>(
            PercentFreightInvoiceRollupAdjuster(19.1),
            RoundingFreightInvoiceRollupAdjuster(1.0),
            MinimumFreightInvoiceRollupAdjuster(41.2),
            FreightInvoiceRollupRatioOffsetAdjuster(1.46),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 33505.8,
    )
}
