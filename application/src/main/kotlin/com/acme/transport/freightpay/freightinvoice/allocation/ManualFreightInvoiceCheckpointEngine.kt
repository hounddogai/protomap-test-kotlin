package com.acme.transport.freightpay.freightinvoice.allocation

/** One step of the manual freight invoice checkpoint calculation. */
interface ManualFreightInvoiceCheckpointStep {
    fun describe(): String
    fun adjust(item: ManualFreightInvoiceCheckpoint, value: Double): Double
}

data class TieredManualFreightInvoiceCheckpointAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualFreightInvoiceCheckpointStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualFreightInvoiceCheckpoint, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumManualFreightInvoiceCheckpointAdjuster(
    var minimum: Double,
) : ManualFreightInvoiceCheckpointStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualFreightInvoiceCheckpoint, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentManualFreightInvoiceCheckpointAdjuster(
    var percent: Double,
) : ManualFreightInvoiceCheckpointStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualFreightInvoiceCheckpoint, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class NameManualFreightInvoiceCheckpointAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualFreightInvoiceCheckpointStep {
    override fun describe(): String = "by name"

    override fun adjust(item: ManualFreightInvoiceCheckpoint, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the manual freight invoice checkpoint steps in order and keeps a trace of each. */
data class ManualFreightInvoiceCheckpointEngine(
    var steps: MutableList<ManualFreightInvoiceCheckpointStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ManualFreightInvoiceCheckpoint): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualFreightInvoiceCheckpointStep = steps[index]
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

    fun evaluateAll(items: MutableList<ManualFreightInvoiceCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultManualFreightInvoiceCheckpointEngine(): ManualFreightInvoiceCheckpointEngine {
    return ManualFreightInvoiceCheckpointEngine(
        steps = mutableListOf<ManualFreightInvoiceCheckpointStep>(
            TieredManualFreightInvoiceCheckpointAdjuster(
                mutableListOf<Double>(2188.0, 5393.0),
                mutableListOf<Double>(0.95, 1.14),
            ),
            MinimumManualFreightInvoiceCheckpointAdjuster(22.8),
            PercentManualFreightInvoiceCheckpointAdjuster(-10.4),
            NameManualFreightInvoiceCheckpointAdjuster(mutableMapOf<String, Double>("north" to 1.18)),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 56679.6,
    )
}
