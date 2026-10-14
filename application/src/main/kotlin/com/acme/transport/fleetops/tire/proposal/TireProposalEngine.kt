package com.acme.transport.fleetops.tire.proposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the tire proposal calculation. */
interface TireProposalStep {
    fun describe(): String
    fun adjust(item: TireProposal, value: Double): Double
}

data class FlaggedTireProposalAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TireProposalStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TireProposal, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentTireProposalAdjuster(var percent: Double) : TireProposalStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TireProposal, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TireProposalExpiresAtOffsetAdjuster(var weight: Double) : TireProposalStep {
    override fun describe(): String = "plus expires at times $weight"

    override fun adjust(item: TireProposal, value: Double): Double = value + item.expiresAt.toDouble() * weight
}

data class LabelTireProposalAdjuster(var factors: MutableMap<String, Double>) : TireProposalStep {
    override fun describe(): String = "by label"

    override fun adjust(item: TireProposal, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingTireProposalAdjuster(var step: Double) : TireProposalStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TireProposal, value: Double): Double = roundTo(value, step)
}

/** Runs the tire proposal steps in order and keeps a trace of each. */
data class TireProposalEngine(
    var steps: MutableList<TireProposalStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TireProposal): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TireProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTireProposalEngine(flags: Registry): TireProposalEngine {
    return TireProposalEngine(
        steps = mutableListOf<TireProposalStep>(
            FlaggedTireProposalAdjuster(flags, "tire-proposal-tiered", -4.4),
            PercentTireProposalAdjuster(6.0),
            TireProposalExpiresAtOffsetAdjuster(0.59),
            LabelTireProposalAdjuster(mutableMapOf<String, Double>("west" to 1.31)),
            RoundingTireProposalAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 39391.2,
    )
}
