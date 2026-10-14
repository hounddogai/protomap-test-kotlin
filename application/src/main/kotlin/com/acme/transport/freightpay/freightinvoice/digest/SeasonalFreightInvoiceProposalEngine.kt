package com.acme.transport.freightpay.freightinvoice.digest

import com.acme.transport.kit.flags.Registry

/** One step of the seasonal freight invoice proposal calculation. */
interface SeasonalFreightInvoiceProposalStep {
    fun describe(): String
    fun adjust(item: SeasonalFreightInvoiceProposal, value: Double): Double
}

data class SeasonalFreightInvoiceProposalBatchSizeOffsetAdjuster(
    var weight: Double,
) : SeasonalFreightInvoiceProposalStep {
    override fun describe(): String = "plus batch size times $weight"

    override fun adjust(item: SeasonalFreightInvoiceProposal, value: Double): Double {
        return value + item.batchSize.toDouble() * weight
    }
}

data class FlaggedSeasonalFreightInvoiceProposalAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalFreightInvoiceProposalStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalFreightInvoiceProposal, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredSeasonalFreightInvoiceProposalAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalFreightInvoiceProposalStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalFreightInvoiceProposal, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapSeasonalFreightInvoiceProposalAdjuster(var cap: Double) : SeasonalFreightInvoiceProposalStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalFreightInvoiceProposal, value: Double): Double = minOf(value, cap)
}

/** Runs the seasonal freight invoice proposal steps in order and keeps a trace of each. */
data class SeasonalFreightInvoiceProposalEngine(
    var steps: MutableList<SeasonalFreightInvoiceProposalStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: SeasonalFreightInvoiceProposal): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalFreightInvoiceProposalStep = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalFreightInvoiceProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultSeasonalFreightInvoiceProposalEngine(flags: Registry): SeasonalFreightInvoiceProposalEngine {
    return SeasonalFreightInvoiceProposalEngine(
        steps = mutableListOf<SeasonalFreightInvoiceProposalStep>(
            SeasonalFreightInvoiceProposalBatchSizeOffsetAdjuster(0.25),
            FlaggedSeasonalFreightInvoiceProposalAdjuster(
                flags,
                "seasonal-freight-invoice-proposal-tiered",
                -8.8,
            ),
            TieredSeasonalFreightInvoiceProposalAdjuster(
                mutableListOf<Double>(5841.0, 6105.0),
                mutableListOf<Double>(1.09, 1.19),
            ),
            CapSeasonalFreightInvoiceProposalAdjuster(17659.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 73966.5,
    )
}
