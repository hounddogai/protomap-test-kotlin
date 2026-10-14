package com.acme.transport.freightpay.freightinvoice.window

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the freight invoice threshold calculation. */
interface FreightInvoiceThresholdFactor {
    fun describe(): String
    fun adjust(item: FreightInvoiceThreshold, value: Double): Double
}

data class TieredFreightInvoiceThresholdAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FreightInvoiceThresholdFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FreightInvoiceThreshold, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentFreightInvoiceThresholdAdjuster(var percent: Double) : FreightInvoiceThresholdFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FreightInvoiceThreshold, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FreightInvoiceThresholdPalletsOffsetAdjuster(var weight: Double) : FreightInvoiceThresholdFactor {
    override fun describe(): String = "plus pallets times $weight"

    override fun adjust(item: FreightInvoiceThreshold, value: Double): Double {
        return value + item.pallets.toDouble() * weight
    }
}

data class MinimumFreightInvoiceThresholdAdjuster(var minimum: Double) : FreightInvoiceThresholdFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FreightInvoiceThreshold, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingFreightInvoiceThresholdAdjuster(var step: Double) : FreightInvoiceThresholdFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FreightInvoiceThreshold, value: Double): Double = roundTo(value, step)
}

/** Runs the freight invoice threshold steps in order and keeps a trace of each. */
data class FreightInvoiceThresholdEngine(
    var steps: MutableList<FreightInvoiceThresholdFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: FreightInvoiceThreshold): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FreightInvoiceThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultFreightInvoiceThresholdEngine(): FreightInvoiceThresholdEngine {
    return FreightInvoiceThresholdEngine(
        steps = mutableListOf<FreightInvoiceThresholdFactor>(
            TieredFreightInvoiceThresholdAdjuster(
                mutableListOf<Double>(3820.0, 4055.0, 4169.0),
                mutableListOf<Double>(1.2, 1.1, 0.89),
            ),
            PercentFreightInvoiceThresholdAdjuster(3.6),
            FreightInvoiceThresholdPalletsOffsetAdjuster(0.77),
            MinimumFreightInvoiceThresholdAdjuster(18.4),
            RoundingFreightInvoiceThresholdAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 69428.9,
    )
}
