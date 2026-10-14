package com.acme.transport.freightpay.freightinvoice.manifest

import com.acme.transport.kit.flags.Registry

/** One step of the freight invoice checkpoint calculation. */
interface FreightInvoiceCheckpointModifier {
    fun describe(): String
    fun adjust(item: FreightInvoiceCheckpoint, value: Double): Double
}

data class FlaggedFreightInvoiceCheckpointAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FreightInvoiceCheckpointModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FreightInvoiceCheckpoint, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredFreightInvoiceCheckpointAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FreightInvoiceCheckpointModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FreightInvoiceCheckpoint, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentFreightInvoiceCheckpointAdjuster(var percent: Double) : FreightInvoiceCheckpointModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FreightInvoiceCheckpoint, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the freight invoice checkpoint steps in order and keeps a trace of each. */
data class FreightInvoiceCheckpointEngine(
    var steps: MutableList<FreightInvoiceCheckpointModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: FreightInvoiceCheckpoint): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FreightInvoiceCheckpointModifier = steps[index]
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

    fun evaluateAll(items: MutableList<FreightInvoiceCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultFreightInvoiceCheckpointEngine(flags: Registry): FreightInvoiceCheckpointEngine {
    return FreightInvoiceCheckpointEngine(
        steps = mutableListOf<FreightInvoiceCheckpointModifier>(
            FlaggedFreightInvoiceCheckpointAdjuster(flags, "freight-invoice-checkpoint-capped", -5.9),
            TieredFreightInvoiceCheckpointAdjuster(
                mutableListOf<Double>(5007.0, 5361.0, 9353.0),
                mutableListOf<Double>(0.96, 1.18, 1.19),
            ),
            PercentFreightInvoiceCheckpointAdjuster(-11.8),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 23158.0,
    )
}
