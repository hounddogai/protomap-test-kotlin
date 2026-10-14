package com.acme.transport.freightpay.freightinvoice.bundle

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the freight invoice request calculation. */
interface FreightInvoiceRequestFactor {
    fun describe(): String
    fun adjust(item: FreightInvoiceRequest, value: Double): Double
}

data class TitleFreightInvoiceRequestAdjuster(
    var factors: MutableMap<String, Double>,
) : FreightInvoiceRequestFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: FreightInvoiceRequest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumFreightInvoiceRequestAdjuster(var minimum: Double) : FreightInvoiceRequestFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FreightInvoiceRequest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredFreightInvoiceRequestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FreightInvoiceRequestFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FreightInvoiceRequest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentFreightInvoiceRequestAdjuster(var percent: Double) : FreightInvoiceRequestFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FreightInvoiceRequest, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the freight invoice request steps in order and keeps a trace of each. */
data class FreightInvoiceRequestEngine(
    var steps: MutableList<FreightInvoiceRequestFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: FreightInvoiceRequest): Double {
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

    fun evaluateAll(items: MutableList<FreightInvoiceRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultFreightInvoiceRequestEngine(): FreightInvoiceRequestEngine {
    return FreightInvoiceRequestEngine(
        steps = mutableListOf<FreightInvoiceRequestFactor>(
            TitleFreightInvoiceRequestAdjuster(
                mutableMapOf<String, Double>("east" to 1.37, "online" to 0.97, "west" to 0.72),
            ),
            MinimumFreightInvoiceRequestAdjuster(13.6),
            TieredFreightInvoiceRequestAdjuster(
                mutableListOf<Double>(1702.0, 3815.0, 9300.0, 9388.0),
                mutableListOf<Double>(0.82, 1.12, 1.1, 1.08),
            ),
            PercentFreightInvoiceRequestAdjuster(-14.6),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 8323.3,
    )
}
