package com.acme.transport.crewing.swap.proposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the swap proposal calculation. */
interface SwapProposalFactor {
    fun describe(): String
    fun adjust(item: SwapProposal, value: Double): Double
}

data class FlaggedSwapProposalAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SwapProposalFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SwapProposal, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumSwapProposalAdjuster(var minimum: Double) : SwapProposalFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SwapProposal, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingSwapProposalAdjuster(var step: Double) : SwapProposalFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SwapProposal, value: Double): Double = roundTo(value, step)
}

/** Runs the swap proposal steps in order and keeps a trace of each. */
data class SwapProposalEngine(
    var steps: MutableList<SwapProposalFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: SwapProposal): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SwapProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultSwapProposalEngine(flags: Registry): SwapProposalEngine {
    return SwapProposalEngine(
        steps = mutableListOf<SwapProposalFactor>(
            FlaggedSwapProposalAdjuster(flags, "swap-proposal-pending", 5.8),
            MinimumSwapProposalAdjuster(3.0),
            RoundingSwapProposalAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 46424.4,
    )
}
