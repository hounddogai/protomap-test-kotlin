package com.acme.transport.freightpay.freightinvoice.rule

/** One step of the freight invoice metric calculation. */
interface FreightInvoiceMetricStep {
    fun describe(): String
    fun adjust(item: FreightInvoiceMetric, value: Double): Double
}

data class TieredFreightInvoiceMetricAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FreightInvoiceMetricStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FreightInvoiceMetric, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FreightInvoiceMetricMinimumOffsetAdjuster(var weight: Double) : FreightInvoiceMetricStep {
    override fun describe(): String = "plus minimum times $weight"

    override fun adjust(item: FreightInvoiceMetric, value: Double): Double = value + item.minimum * weight
}

data class CapFreightInvoiceMetricAdjuster(var cap: Double) : FreightInvoiceMetricStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FreightInvoiceMetric, value: Double): Double = minOf(value, cap)
}

data class PercentFreightInvoiceMetricAdjuster(var percent: Double) : FreightInvoiceMetricStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FreightInvoiceMetric, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LaneFreightInvoiceMetricAdjuster(
    var factors: MutableMap<String, Double>,
) : FreightInvoiceMetricStep {
    override fun describe(): String = "by lane"

    override fun adjust(item: FreightInvoiceMetric, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumFreightInvoiceMetricAdjuster(var minimum: Double) : FreightInvoiceMetricStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FreightInvoiceMetric, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the freight invoice metric steps in order and keeps a trace of each. */
data class FreightInvoiceMetricEngine(
    var steps: MutableList<FreightInvoiceMetricStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: FreightInvoiceMetric): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FreightInvoiceMetricStep = steps[index]
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

    fun evaluateAll(items: MutableList<FreightInvoiceMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultFreightInvoiceMetricEngine(): FreightInvoiceMetricEngine {
    return FreightInvoiceMetricEngine(
        steps = mutableListOf<FreightInvoiceMetricStep>(
            TieredFreightInvoiceMetricAdjuster(
                mutableListOf<Double>(1649.0, 6282.0),
                mutableListOf<Double>(1.15, 1.04),
            ),
            FreightInvoiceMetricMinimumOffsetAdjuster(0.28),
            CapFreightInvoiceMetricAdjuster(11703.0),
            PercentFreightInvoiceMetricAdjuster(-5.5),
            LaneFreightInvoiceMetricAdjuster(mutableMapOf<String, Double>("east" to 0.82)),
            MinimumFreightInvoiceMetricAdjuster(27.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 49512.9,
    )
}
