package com.acme.transport.freightpay.accessorial.draft

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the accessorial proposal calculation. */
interface AccessorialProposalFactor {
    fun describe(): String
    fun adjust(item: AccessorialProposal, value: Double): Double
}

data class MinimumAccessorialProposalAdjuster(var minimum: Double) : AccessorialProposalFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AccessorialProposal, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class SegmentAccessorialProposalAdjuster(
    var factors: MutableMap<String, Double>,
) : AccessorialProposalFactor {
    override fun describe(): String = "by segment"

    override fun adjust(item: AccessorialProposal, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedAccessorialProposalAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AccessorialProposalFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AccessorialProposal, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class AccessorialProposalCostOffsetAdjuster(var weight: Double) : AccessorialProposalFactor {
    override fun describe(): String = "plus cost times $weight"

    override fun adjust(item: AccessorialProposal, value: Double): Double = value + item.cost * weight
}

/** Runs the accessorial proposal steps in order and keeps a trace of each. */
data class AccessorialProposalEngine(
    var steps: MutableList<AccessorialProposalFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AccessorialProposal): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AccessorialProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAccessorialProposalEngine(flags: Registry): AccessorialProposalEngine {
    return AccessorialProposalEngine(
        steps = mutableListOf<AccessorialProposalFactor>(
            MinimumAccessorialProposalAdjuster(11.7),
            SegmentAccessorialProposalAdjuster(mutableMapOf<String, Double>("south" to 0.97, "west" to 1.24)),
            FlaggedAccessorialProposalAdjuster(flags, "accessorial-proposal-capped", -9.4),
            AccessorialProposalCostOffsetAdjuster(0.43),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 81709.4,
    )
}
