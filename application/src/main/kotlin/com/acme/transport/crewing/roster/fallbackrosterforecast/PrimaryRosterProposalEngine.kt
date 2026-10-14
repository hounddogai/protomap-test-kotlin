package com.acme.transport.crewing.roster.fallbackrosterforecast

import com.acme.transport.kit.flags.Registry

/** One step of the primary roster proposal calculation. */
interface PrimaryRosterProposalAdjuster {
    fun describe(): String
    fun adjust(item: PrimaryRosterProposal, value: Double): Double
}

data class TieredPrimaryRosterProposalAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryRosterProposalAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryRosterProposal, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentPrimaryRosterProposalAdjuster(var percent: Double) : PrimaryRosterProposalAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryRosterProposal, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedPrimaryRosterProposalAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryRosterProposalAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryRosterProposal, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PrimaryRosterProposalVersionOffsetAdjuster(var weight: Double) : PrimaryRosterProposalAdjuster {
    override fun describe(): String = "plus version times $weight"

    override fun adjust(item: PrimaryRosterProposal, value: Double): Double = value + item.version.toDouble() * weight
}

/** Runs the primary roster proposal steps in order and keeps a trace of each. */
data class PrimaryRosterProposalEngine(
    var steps: MutableList<PrimaryRosterProposalAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PrimaryRosterProposal): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryRosterProposalAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryRosterProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPrimaryRosterProposalEngine(flags: Registry): PrimaryRosterProposalEngine {
    return PrimaryRosterProposalEngine(
        steps = mutableListOf<PrimaryRosterProposalAdjuster>(
            TieredPrimaryRosterProposalAdjuster(
                mutableListOf<Double>(956.0, 3312.0, 4194.0, 4209.0),
                mutableListOf<Double>(0.91, 1.1, 0.89, 1.01),
            ),
            PercentPrimaryRosterProposalAdjuster(-12.8),
            FlaggedPrimaryRosterProposalAdjuster(flags, "primary-roster-proposal-seasonal", -7.9),
            PrimaryRosterProposalVersionOffsetAdjuster(0.76),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 1649.9,
    )
}
