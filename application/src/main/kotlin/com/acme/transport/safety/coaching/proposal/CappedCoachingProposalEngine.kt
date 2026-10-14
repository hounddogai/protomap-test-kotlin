package com.acme.transport.safety.coaching.proposal

import com.acme.transport.kit.numbers.roundTo

/** One step of the capped coaching proposal calculation. */
interface CappedCoachingProposalStep {
    fun describe(): String
    fun adjust(item: CappedCoachingProposal, value: Double): Double
}

data class PercentCappedCoachingProposalAdjuster(var percent: Double) : CappedCoachingProposalStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedCoachingProposal, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapCappedCoachingProposalAdjuster(var cap: Double) : CappedCoachingProposalStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedCoachingProposal, value: Double): Double = minOf(value, cap)
}

data class RoundingCappedCoachingProposalAdjuster(var step: Double) : CappedCoachingProposalStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedCoachingProposal, value: Double): Double = roundTo(value, step)
}

data class CappedCoachingProposalWindowDaysOffsetAdjuster(var weight: Double) : CappedCoachingProposalStep {
    override fun describe(): String = "plus window days times $weight"

    override fun adjust(item: CappedCoachingProposal, value: Double): Double {
        return value + item.windowDays.toDouble() * weight
    }
}

data class LabelCappedCoachingProposalAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedCoachingProposalStep {
    override fun describe(): String = "by label"

    override fun adjust(item: CappedCoachingProposal, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredCappedCoachingProposalAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedCoachingProposalStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedCoachingProposal, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the capped coaching proposal steps in order and keeps a trace of each. */
data class CappedCoachingProposalEngine(
    var steps: MutableList<CappedCoachingProposalStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CappedCoachingProposal): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedCoachingProposalStep = steps[index]
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

    fun evaluateAll(items: MutableList<CappedCoachingProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCappedCoachingProposalEngine(): CappedCoachingProposalEngine {
    return CappedCoachingProposalEngine(
        steps = mutableListOf<CappedCoachingProposalStep>(
            PercentCappedCoachingProposalAdjuster(-5.3),
            CapCappedCoachingProposalAdjuster(33795.0),
            RoundingCappedCoachingProposalAdjuster(1.0),
            CappedCoachingProposalWindowDaysOffsetAdjuster(0.02),
            LabelCappedCoachingProposalAdjuster(mutableMapOf<String, Double>("central" to 1.37, "east" to 1.24)),
            TieredCappedCoachingProposalAdjuster(
                mutableListOf<Double>(1848.0, 7827.0, 8609.0, 9687.0),
                mutableListOf<Double>(0.94, 1.17, 1.14, 0.88),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 16908.4,
    )
}
