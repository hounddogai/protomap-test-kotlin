package com.acme.transport.crewing.roster.summary

import com.acme.transport.kit.flags.Registry

/** One step of the roster proposal calculation. */
interface RosterProposalFactor {
    fun describe(): String
    fun adjust(item: RosterProposal, value: Double): Double
}

data class RosterProposalExpiresAtOffsetAdjuster(var weight: Double) : RosterProposalFactor {
    override fun describe(): String = "plus expires at times $weight"

    override fun adjust(item: RosterProposal, value: Double): Double = value + item.expiresAt.toDouble() * weight
}

data class CapRosterProposalAdjuster(var cap: Double) : RosterProposalFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RosterProposal, value: Double): Double = minOf(value, cap)
}

data class FlaggedRosterProposalAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RosterProposalFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RosterProposal, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredRosterProposalAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RosterProposalFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RosterProposal, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the roster proposal steps in order and keeps a trace of each. */
data class RosterProposalEngine(
    var steps: MutableList<RosterProposalFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RosterProposal): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RosterProposalFactor = steps[index]
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

    fun evaluateAll(items: MutableList<RosterProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRosterProposalEngine(flags: Registry): RosterProposalEngine {
    return RosterProposalEngine(
        steps = mutableListOf<RosterProposalFactor>(
            RosterProposalExpiresAtOffsetAdjuster(1.94),
            CapRosterProposalAdjuster(9184.0),
            FlaggedRosterProposalAdjuster(flags, "roster-proposal-global", -3.3),
            TieredRosterProposalAdjuster(
                mutableListOf<Double>(1253.0, 5034.0),
                mutableListOf<Double>(1.07, 1.14),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 72485.1,
    )
}
