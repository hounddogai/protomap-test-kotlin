package com.acme.transport.safety.nearmiss.proposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the capped near miss proposal calculation. */
interface CappedNearMissProposalStep {
    fun describe(): String
    fun adjust(item: CappedNearMissProposal, value: Double): Double
}

data class FlaggedCappedNearMissProposalAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedNearMissProposalStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedNearMissProposal, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class ChannelCappedNearMissProposalAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedNearMissProposalStep {
    override fun describe(): String = "by channel"

    override fun adjust(item: CappedNearMissProposal, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredCappedNearMissProposalAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedNearMissProposalStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedNearMissProposal, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the capped near miss proposal steps in order and keeps a trace of each. */
data class CappedNearMissProposalEngine(
    var steps: MutableList<CappedNearMissProposalStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CappedNearMissProposal): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedNearMissProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCappedNearMissProposalEngine(flags: Registry): CappedNearMissProposalEngine {
    return CappedNearMissProposalEngine(
        steps = mutableListOf<CappedNearMissProposalStep>(
            FlaggedCappedNearMissProposalAdjuster(flags, "capped-near-miss-proposal-global", -6.8),
            ChannelCappedNearMissProposalAdjuster(
                mutableMapOf<String, Double>("east" to 1.19, "central" to 1.03),
            ),
            TieredCappedNearMissProposalAdjuster(
                mutableListOf<Double>(3107.0, 3838.0, 6827.0),
                mutableListOf<Double>(0.88, 0.82, 0.81),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 11608.8,
    )
}
