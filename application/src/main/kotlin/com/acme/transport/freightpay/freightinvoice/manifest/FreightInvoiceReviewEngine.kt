package com.acme.transport.freightpay.freightinvoice.manifest

/** One step of the freight invoice review calculation. */
interface FreightInvoiceReviewStep {
    fun describe(): String
    fun adjust(item: FreightInvoiceReview, value: Double): Double
}

data class MinimumFreightInvoiceReviewAdjuster(var minimum: Double) : FreightInvoiceReviewStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FreightInvoiceReview, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FreightInvoiceReviewGraceDaysOffsetAdjuster(var weight: Double) : FreightInvoiceReviewStep {
    override fun describe(): String = "plus grace days times $weight"

    override fun adjust(item: FreightInvoiceReview, value: Double): Double {
        return value + item.graceDays.toDouble() * weight
    }
}

data class TieredFreightInvoiceReviewAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FreightInvoiceReviewStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FreightInvoiceReview, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the freight invoice review steps in order and keeps a trace of each. */
data class FreightInvoiceReviewEngine(
    var steps: MutableList<FreightInvoiceReviewStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: FreightInvoiceReview): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FreightInvoiceReviewStep = steps[index]
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

    fun evaluateAll(items: MutableList<FreightInvoiceReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultFreightInvoiceReviewEngine(): FreightInvoiceReviewEngine {
    return FreightInvoiceReviewEngine(
        steps = mutableListOf<FreightInvoiceReviewStep>(
            MinimumFreightInvoiceReviewAdjuster(37.5),
            FreightInvoiceReviewGraceDaysOffsetAdjuster(1.42),
            TieredFreightInvoiceReviewAdjuster(
                mutableListOf<Double>(1953.0, 9081.0),
                mutableListOf<Double>(1.05, 0.91),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 39608.1,
    )
}
