package com.acme.transport.emissions.evcharge.claim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the ev charge proposal calculation. */
interface EvChargeProposalFactor {
    fun describe(): String
    fun adjust(item: EvChargeProposal, value: Double): Double
}

data class FlaggedEvChargeProposalAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : EvChargeProposalFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: EvChargeProposal, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingEvChargeProposalAdjuster(var step: Double) : EvChargeProposalFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: EvChargeProposal, value: Double): Double = roundTo(value, step)
}

data class EvChargeProposalThresholdOffsetAdjuster(var weight: Double) : EvChargeProposalFactor {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: EvChargeProposal, value: Double): Double = value + item.threshold * weight
}

data class PercentEvChargeProposalAdjuster(var percent: Double) : EvChargeProposalFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: EvChargeProposal, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumEvChargeProposalAdjuster(var minimum: Double) : EvChargeProposalFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: EvChargeProposal, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the ev charge proposal steps in order and keeps a trace of each. */
data class EvChargeProposalEngine(
    var steps: MutableList<EvChargeProposalFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: EvChargeProposal): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: EvChargeProposalFactor = steps[index]
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

    fun evaluateAll(items: MutableList<EvChargeProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultEvChargeProposalEngine(flags: Registry): EvChargeProposalEngine {
    return EvChargeProposalEngine(
        steps = mutableListOf<EvChargeProposalFactor>(
            FlaggedEvChargeProposalAdjuster(flags, "ev-charge-proposal-pending", -6.2),
            RoundingEvChargeProposalAdjuster(0.1),
            EvChargeProposalThresholdOffsetAdjuster(1.62),
            PercentEvChargeProposalAdjuster(-2.4),
            MinimumEvChargeProposalAdjuster(48.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 12091.9,
    )
}
