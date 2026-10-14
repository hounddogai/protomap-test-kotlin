package com.acme.transport.lastmile.redelivery.redeliveryproposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the legacy redelivery proposal calculation. */
interface LegacyRedeliveryProposalAdjuster {
    fun describe(): String
    fun adjust(item: LegacyRedeliveryProposal, value: Double): Double
}

data class RoundingLegacyRedeliveryProposalAdjuster(var step: Double) : LegacyRedeliveryProposalAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyRedeliveryProposal, value: Double): Double = roundTo(value, step)
}

data class TieredLegacyRedeliveryProposalAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyRedeliveryProposalAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyRedeliveryProposal, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedLegacyRedeliveryProposalAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyRedeliveryProposalAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyRedeliveryProposal, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the legacy redelivery proposal steps in order and keeps a trace of each. */
data class LegacyRedeliveryProposalEngine(
    var steps: MutableList<LegacyRedeliveryProposalAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LegacyRedeliveryProposal): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyRedeliveryProposalAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyRedeliveryProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLegacyRedeliveryProposalEngine(flags: Registry): LegacyRedeliveryProposalEngine {
    return LegacyRedeliveryProposalEngine(
        steps = mutableListOf<LegacyRedeliveryProposalAdjuster>(
            RoundingLegacyRedeliveryProposalAdjuster(0.5),
            TieredLegacyRedeliveryProposalAdjuster(
                mutableListOf<Double>(187.0, 5719.0),
                mutableListOf<Double>(0.98, 1.14),
            ),
            FlaggedLegacyRedeliveryProposalAdjuster(flags, "legacy-redelivery-proposal-global", -4.9),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 22498.5,
    )
}
