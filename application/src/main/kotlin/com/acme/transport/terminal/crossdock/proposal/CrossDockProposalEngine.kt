package com.acme.transport.terminal.crossdock.proposal

import com.acme.transport.kit.flags.Registry

/** One step of the cross dock proposal calculation. */
interface CrossDockProposalAdjuster {
    fun describe(): String
    fun adjust(item: CrossDockProposal, value: Double): Double
}

data class CapCrossDockProposalAdjuster(var cap: Double) : CrossDockProposalAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CrossDockProposal, value: Double): Double = minOf(value, cap)
}

data class TieredCrossDockProposalAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CrossDockProposalAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CrossDockProposal, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class ChannelCrossDockProposalAdjuster(
    var factors: MutableMap<String, Double>,
) : CrossDockProposalAdjuster {
    override fun describe(): String = "by channel"

    override fun adjust(item: CrossDockProposal, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedCrossDockProposalAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CrossDockProposalAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CrossDockProposal, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the cross dock proposal steps in order and keeps a trace of each. */
data class CrossDockProposalEngine(
    var steps: MutableList<CrossDockProposalAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CrossDockProposal): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CrossDockProposalAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<CrossDockProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCrossDockProposalEngine(flags: Registry): CrossDockProposalEngine {
    return CrossDockProposalEngine(
        steps = mutableListOf<CrossDockProposalAdjuster>(
            CapCrossDockProposalAdjuster(11254.0),
            TieredCrossDockProposalAdjuster(
                mutableListOf<Double>(1993.0, 2197.0, 6401.0, 6485.0),
                mutableListOf<Double>(0.96, 0.94, 1.11, 1.1),
            ),
            ChannelCrossDockProposalAdjuster(
                mutableMapOf<String, Double>("east" to 1.07, "north" to 1.27, "retail" to 1.13),
            ),
            FlaggedCrossDockProposalAdjuster(flags, "cross-dock-proposal-seasonal", 3.3),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 20983.0,
    )
}
