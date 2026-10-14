package com.acme.transport.freightpay.freightinvoice.invoicesummary

/** One step of the pending freight invoice summary calculation. */
interface PendingFreightInvoiceSummaryModifier {
    fun describe(): String
    fun adjust(item: PendingFreightInvoiceSummary, value: Double): Double
}

data class CategoryPendingFreightInvoiceSummaryAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingFreightInvoiceSummaryModifier {
    override fun describe(): String = "by category"

    override fun adjust(item: PendingFreightInvoiceSummary, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredPendingFreightInvoiceSummaryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingFreightInvoiceSummaryModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingFreightInvoiceSummary, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentPendingFreightInvoiceSummaryAdjuster(
    var percent: Double,
) : PendingFreightInvoiceSummaryModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingFreightInvoiceSummary, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapPendingFreightInvoiceSummaryAdjuster(var cap: Double) : PendingFreightInvoiceSummaryModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingFreightInvoiceSummary, value: Double): Double = minOf(value, cap)
}

data class PendingFreightInvoiceSummaryBatchSizeOffsetAdjuster(
    var weight: Double,
) : PendingFreightInvoiceSummaryModifier {
    override fun describe(): String = "plus batch size times $weight"

    override fun adjust(item: PendingFreightInvoiceSummary, value: Double): Double {
        return value + item.batchSize.toDouble() * weight
    }
}

data class MinimumPendingFreightInvoiceSummaryAdjuster(
    var minimum: Double,
) : PendingFreightInvoiceSummaryModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingFreightInvoiceSummary, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the pending freight invoice summary steps in order and keeps a trace of each. */
data class PendingFreightInvoiceSummaryEngine(
    var steps: MutableList<PendingFreightInvoiceSummaryModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PendingFreightInvoiceSummary): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingFreightInvoiceSummaryModifier = steps[index]
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

    fun evaluateAll(items: MutableList<PendingFreightInvoiceSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPendingFreightInvoiceSummaryEngine(): PendingFreightInvoiceSummaryEngine {
    return PendingFreightInvoiceSummaryEngine(
        steps = mutableListOf<PendingFreightInvoiceSummaryModifier>(
            CategoryPendingFreightInvoiceSummaryAdjuster(
                mutableMapOf<String, Double>("south" to 0.71, "east" to 1.01),
            ),
            TieredPendingFreightInvoiceSummaryAdjuster(
                mutableListOf<Double>(2316.0, 7495.0, 7643.0, 8317.0),
                mutableListOf<Double>(1.16, 1.1, 0.9, 1.2),
            ),
            PercentPendingFreightInvoiceSummaryAdjuster(2.1),
            CapPendingFreightInvoiceSummaryAdjuster(8489.0),
            PendingFreightInvoiceSummaryBatchSizeOffsetAdjuster(1.31),
            MinimumPendingFreightInvoiceSummaryAdjuster(19.2),
        ),
        trace = mutableListOf<String>(),
        floor = 5.0,
        ceiling = 14392.8,
    )
}
