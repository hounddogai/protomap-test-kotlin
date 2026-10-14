package com.acme.transport.terminal.dwell.proposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the seasonal dwell proposal calculation. */
interface SeasonalDwellProposalAdjuster {
    fun describe(): String
    fun adjust(item: SeasonalDwellProposal, value: Double): Double
}

data class RoundingSeasonalDwellProposalAdjuster(var step: Double) : SeasonalDwellProposalAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalDwellProposal, value: Double): Double = roundTo(value, step)
}

data class FlaggedSeasonalDwellProposalAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalDwellProposalAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalDwellProposal, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapSeasonalDwellProposalAdjuster(var cap: Double) : SeasonalDwellProposalAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalDwellProposal, value: Double): Double = minOf(value, cap)
}

/** Runs the seasonal dwell proposal steps in order and keeps a trace of each. */
data class SeasonalDwellProposalEngine(
    var steps: MutableList<SeasonalDwellProposalAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: SeasonalDwellProposal): Double {
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

    fun evaluateAll(items: MutableList<SeasonalDwellProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultSeasonalDwellProposalEngine(flags: Registry): SeasonalDwellProposalEngine {
    return SeasonalDwellProposalEngine(
        steps = mutableListOf<SeasonalDwellProposalAdjuster>(
            RoundingSeasonalDwellProposalAdjuster(0.5),
            FlaggedSeasonalDwellProposalAdjuster(flags, "seasonal-dwell-proposal-global", 4.1),
            CapSeasonalDwellProposalAdjuster(11219.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 77597.5,
    )
}
