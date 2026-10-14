package com.acme.transport.freightpay.accessorial.overrideitem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the seasonal accessorial proposal calculation. */
interface SeasonalAccessorialProposalAdjuster {
    fun describe(): String
    fun adjust(item: SeasonalAccessorialProposal, value: Double): Double
}

data class PercentSeasonalAccessorialProposalAdjuster(
    var percent: Double,
) : SeasonalAccessorialProposalAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalAccessorialProposal, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedSeasonalAccessorialProposalAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalAccessorialProposalAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalAccessorialProposal, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumSeasonalAccessorialProposalAdjuster(
    var minimum: Double,
) : SeasonalAccessorialProposalAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalAccessorialProposal, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the seasonal accessorial proposal steps in order and keeps a trace of each. */
data class SeasonalAccessorialProposalEngine(
    var steps: MutableList<SeasonalAccessorialProposalAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SeasonalAccessorialProposal): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SeasonalAccessorialProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSeasonalAccessorialProposalEngine(flags: Registry): SeasonalAccessorialProposalEngine {
    return SeasonalAccessorialProposalEngine(
        steps = mutableListOf<SeasonalAccessorialProposalAdjuster>(
            PercentSeasonalAccessorialProposalAdjuster(12.5),
            FlaggedSeasonalAccessorialProposalAdjuster(flags, "seasonal-accessorial-proposal-lenient", -0.9),
            MinimumSeasonalAccessorialProposalAdjuster(39.3),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 25739.1,
    )
}
