package com.acme.transport.freightpay.freightinvoice.adjustment

/** One step of the freight invoice adjustment calculation. */
interface FreightInvoiceAdjustmentAdjuster {
    fun describe(): String
    fun adjust(item: FreightInvoiceAdjustment, value: Double): Double
}

data class CategoryFreightInvoiceAdjustmentAdjuster(
    var factors: MutableMap<String, Double>,
) : FreightInvoiceAdjustmentAdjuster {
    override fun describe(): String = "by category"

    override fun adjust(item: FreightInvoiceAdjustment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentFreightInvoiceAdjustmentAdjuster(var percent: Double) : FreightInvoiceAdjustmentAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FreightInvoiceAdjustment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapFreightInvoiceAdjustmentAdjuster(var cap: Double) : FreightInvoiceAdjustmentAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FreightInvoiceAdjustment, value: Double): Double = minOf(value, cap)
}

/** Runs the freight invoice adjustment steps in order and keeps a trace of each. */
data class FreightInvoiceAdjustmentEngine(
    var steps: MutableList<FreightInvoiceAdjustmentAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: FreightInvoiceAdjustment): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FreightInvoiceAdjustmentAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<FreightInvoiceAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultFreightInvoiceAdjustmentEngine(): FreightInvoiceAdjustmentEngine {
    return FreightInvoiceAdjustmentEngine(
        steps = mutableListOf<FreightInvoiceAdjustmentAdjuster>(
            CategoryFreightInvoiceAdjustmentAdjuster(
                mutableMapOf<String, Double>("south" to 0.89, "west" to 1.23, "online" to 0.94),
            ),
            PercentFreightInvoiceAdjustmentAdjuster(-18.5),
            CapFreightInvoiceAdjustmentAdjuster(23048.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 49196.0,
    )
}
