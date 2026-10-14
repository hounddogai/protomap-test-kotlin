package com.acme.transport.terminal.dockdoor.recorditem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the regional dock door proposal calculation. */
interface RegionalDockDoorProposalFactor {
    fun describe(): String
    fun adjust(item: RegionalDockDoorProposal, value: Double): Double
}

data class FlaggedRegionalDockDoorProposalAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalDockDoorProposalFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalDockDoorProposal, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentRegionalDockDoorProposalAdjuster(var percent: Double) : RegionalDockDoorProposalFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalDockDoorProposal, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RegionalDockDoorProposalPriorityOffsetAdjuster(var weight: Double) : RegionalDockDoorProposalFactor {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: RegionalDockDoorProposal, value: Double): Double {
        return value + item.priority.toDouble() * weight
    }
}

data class ChannelRegionalDockDoorProposalAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalDockDoorProposalFactor {
    override fun describe(): String = "by channel"

    override fun adjust(item: RegionalDockDoorProposal, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumRegionalDockDoorProposalAdjuster(var minimum: Double) : RegionalDockDoorProposalFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalDockDoorProposal, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the regional dock door proposal steps in order and keeps a trace of each. */
data class RegionalDockDoorProposalEngine(
    var steps: MutableList<RegionalDockDoorProposalFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RegionalDockDoorProposal): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RegionalDockDoorProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRegionalDockDoorProposalEngine(flags: Registry): RegionalDockDoorProposalEngine {
    return RegionalDockDoorProposalEngine(
        steps = mutableListOf<RegionalDockDoorProposalFactor>(
            FlaggedRegionalDockDoorProposalAdjuster(flags, "regional-dock-door-proposal-capped", -3.1),
            PercentRegionalDockDoorProposalAdjuster(-15.6),
            RegionalDockDoorProposalPriorityOffsetAdjuster(0.92),
            ChannelRegionalDockDoorProposalAdjuster(
                mutableMapOf<String, Double>("north" to 1.04, "online" to 1.15),
            ),
            MinimumRegionalDockDoorProposalAdjuster(16.8),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 44024.2,
    )
}
