package com.acme.transport.customs.hold.proposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the hold proposal calculation. */
interface HoldProposalStep {
    fun describe(): String
    fun adjust(item: HoldProposal, value: Double): Double
}

data class CapHoldProposalAdjuster(var cap: Double) : HoldProposalStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: HoldProposal, value: Double): Double = minOf(value, cap)
}

data class FlaggedHoldProposalAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : HoldProposalStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: HoldProposal, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingHoldProposalAdjuster(var step: Double) : HoldProposalStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: HoldProposal, value: Double): Double = roundTo(value, step)
}

data class HoldProposalStopCountOffsetAdjuster(var weight: Double) : HoldProposalStep {
    override fun describe(): String = "plus stop count times $weight"

    override fun adjust(item: HoldProposal, value: Double): Double = value + item.stopCount.toDouble() * weight
}

data class PercentHoldProposalAdjuster(var percent: Double) : HoldProposalStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: HoldProposal, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumHoldProposalAdjuster(var minimum: Double) : HoldProposalStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: HoldProposal, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the hold proposal steps in order and keeps a trace of each. */
data class HoldProposalEngine(
    var steps: MutableList<HoldProposalStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: HoldProposal): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<HoldProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultHoldProposalEngine(flags: Registry): HoldProposalEngine {
    return HoldProposalEngine(
        steps = mutableListOf<HoldProposalStep>(
            CapHoldProposalAdjuster(730.0),
            FlaggedHoldProposalAdjuster(flags, "hold-proposal-lenient", 9.5),
            RoundingHoldProposalAdjuster(0.5),
            HoldProposalStopCountOffsetAdjuster(0.28),
            PercentHoldProposalAdjuster(2.4),
            MinimumHoldProposalAdjuster(12.9),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 71727.8,
    )
}
