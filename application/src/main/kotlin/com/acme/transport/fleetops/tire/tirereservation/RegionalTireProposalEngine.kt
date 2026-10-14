package com.acme.transport.fleetops.tire.tirereservation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the regional tire proposal calculation. */
interface RegionalTireProposalModifier {
    fun describe(): String
    fun adjust(item: RegionalTireProposal, value: Double): Double
}

data class NameRegionalTireProposalAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalTireProposalModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: RegionalTireProposal, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RegionalTireProposalCostOffsetAdjuster(var weight: Double) : RegionalTireProposalModifier {
    override fun describe(): String = "plus cost times $weight"

    override fun adjust(item: RegionalTireProposal, value: Double): Double = value + item.cost * weight
}

data class MinimumRegionalTireProposalAdjuster(var minimum: Double) : RegionalTireProposalModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalTireProposal, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedRegionalTireProposalAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalTireProposalModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalTireProposal, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapRegionalTireProposalAdjuster(var cap: Double) : RegionalTireProposalModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalTireProposal, value: Double): Double = minOf(value, cap)
}

/** Runs the regional tire proposal steps in order and keeps a trace of each. */
data class RegionalTireProposalEngine(
    var steps: MutableList<RegionalTireProposalModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RegionalTireProposal): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RegionalTireProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRegionalTireProposalEngine(flags: Registry): RegionalTireProposalEngine {
    return RegionalTireProposalEngine(
        steps = mutableListOf<RegionalTireProposalModifier>(
            NameRegionalTireProposalAdjuster(mutableMapOf<String, Double>("central" to 1.15)),
            RegionalTireProposalCostOffsetAdjuster(0.95),
            MinimumRegionalTireProposalAdjuster(14.3),
            FlaggedRegionalTireProposalAdjuster(flags, "regional-tire-proposal-global", -0.8),
            CapRegionalTireProposalAdjuster(33605.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 14908.2,
    )
}
