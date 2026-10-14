package com.acme.transport.network.hub.overrideitem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the hub proposal calculation. */
interface HubProposalStep {
    fun describe(): String
    fun adjust(item: HubProposal, value: Double): Double
}

data class FlaggedHubProposalAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : HubProposalStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: HubProposal, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LocaleHubProposalAdjuster(var factors: MutableMap<String, Double>) : HubProposalStep {
    override fun describe(): String = "by locale"

    override fun adjust(item: HubProposal, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingHubProposalAdjuster(var step: Double) : HubProposalStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: HubProposal, value: Double): Double = roundTo(value, step)
}

data class TieredHubProposalAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : HubProposalStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: HubProposal, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the hub proposal steps in order and keeps a trace of each. */
data class HubProposalEngine(
    var steps: MutableList<HubProposalStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: HubProposal): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: HubProposalStep = steps[index]
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

    fun evaluateAll(items: MutableList<HubProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultHubProposalEngine(flags: Registry): HubProposalEngine {
    return HubProposalEngine(
        steps = mutableListOf<HubProposalStep>(
            FlaggedHubProposalAdjuster(flags, "hub-proposal-tiered", 5.4),
            LocaleHubProposalAdjuster(mutableMapOf<String, Double>("north" to 1.12, "central" to 0.83)),
            RoundingHubProposalAdjuster(0.1),
            TieredHubProposalAdjuster(
                mutableListOf<Double>(2307.0, 4495.0, 6316.0, 8481.0),
                mutableListOf<Double>(1.11, 1.16, 1.02, 1.05),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 31304.5,
    )
}
