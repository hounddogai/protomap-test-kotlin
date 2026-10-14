package com.acme.transport.freightpay.freightinvoice.proposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the freight invoice proposal calculation. */
interface FreightInvoiceProposalStep {
    fun describe(): String
    fun adjust(item: FreightInvoiceProposal, value: Double): Double
}

data class RoundingFreightInvoiceProposalAdjuster(var step: Double) : FreightInvoiceProposalStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FreightInvoiceProposal, value: Double): Double = roundTo(value, step)
}

data class FlaggedFreightInvoiceProposalAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FreightInvoiceProposalStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FreightInvoiceProposal, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class SegmentFreightInvoiceProposalAdjuster(
    var factors: MutableMap<String, Double>,
) : FreightInvoiceProposalStep {
    override fun describe(): String = "by segment"

    override fun adjust(item: FreightInvoiceProposal, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapFreightInvoiceProposalAdjuster(var cap: Double) : FreightInvoiceProposalStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FreightInvoiceProposal, value: Double): Double = minOf(value, cap)
}

/** Runs the freight invoice proposal steps in order and keeps a trace of each. */
data class FreightInvoiceProposalEngine(
    var steps: MutableList<FreightInvoiceProposalStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: FreightInvoiceProposal): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FreightInvoiceProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultFreightInvoiceProposalEngine(flags: Registry): FreightInvoiceProposalEngine {
    return FreightInvoiceProposalEngine(
        steps = mutableListOf<FreightInvoiceProposalStep>(
            RoundingFreightInvoiceProposalAdjuster(0.1),
            FlaggedFreightInvoiceProposalAdjuster(flags, "freight-invoice-proposal-global", -1.5),
            SegmentFreightInvoiceProposalAdjuster(
                mutableMapOf<String, Double>("central" to 1.3, "online" to 1.1, "retail" to 1.37),
            ),
            CapFreightInvoiceProposalAdjuster(4674.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 34827.9,
    )
}
