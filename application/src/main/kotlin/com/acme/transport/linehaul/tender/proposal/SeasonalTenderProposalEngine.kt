package com.acme.transport.linehaul.tender.proposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the seasonal tender proposal calculation. */
interface SeasonalTenderProposalStep {
    fun describe(): String
    fun adjust(item: SeasonalTenderProposal, value: Double): Double
}

data class FlaggedSeasonalTenderProposalAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalTenderProposalStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalTenderProposal, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapSeasonalTenderProposalAdjuster(var cap: Double) : SeasonalTenderProposalStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalTenderProposal, value: Double): Double = minOf(value, cap)
}

data class RoundingSeasonalTenderProposalAdjuster(var step: Double) : SeasonalTenderProposalStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalTenderProposal, value: Double): Double = roundTo(value, step)
}

/** Runs the seasonal tender proposal steps in order and keeps a trace of each. */
data class SeasonalTenderProposalEngine(
    var steps: MutableList<SeasonalTenderProposalStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: SeasonalTenderProposal): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalTenderProposalStep = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalTenderProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultSeasonalTenderProposalEngine(flags: Registry): SeasonalTenderProposalEngine {
    return SeasonalTenderProposalEngine(
        steps = mutableListOf<SeasonalTenderProposalStep>(
            FlaggedSeasonalTenderProposalAdjuster(flags, "seasonal-tender-proposal-weighted", 7.8),
            CapSeasonalTenderProposalAdjuster(3321.0),
            RoundingSeasonalTenderProposalAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 15703.0,
    )
}
