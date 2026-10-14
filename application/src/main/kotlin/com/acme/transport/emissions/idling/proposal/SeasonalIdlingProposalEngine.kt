package com.acme.transport.emissions.idling.proposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the seasonal idling proposal calculation. */
interface SeasonalIdlingProposalModifier {
    fun describe(): String
    fun adjust(item: SeasonalIdlingProposal, value: Double): Double
}

data class TieredSeasonalIdlingProposalAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalIdlingProposalModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalIdlingProposal, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedSeasonalIdlingProposalAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalIdlingProposalModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalIdlingProposal, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumSeasonalIdlingProposalAdjuster(var minimum: Double) : SeasonalIdlingProposalModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalIdlingProposal, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the seasonal idling proposal steps in order and keeps a trace of each. */
data class SeasonalIdlingProposalEngine(
    var steps: MutableList<SeasonalIdlingProposalModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: SeasonalIdlingProposal): Double {
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

    fun evaluateAll(items: MutableList<SeasonalIdlingProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultSeasonalIdlingProposalEngine(flags: Registry): SeasonalIdlingProposalEngine {
    return SeasonalIdlingProposalEngine(
        steps = mutableListOf<SeasonalIdlingProposalModifier>(
            TieredSeasonalIdlingProposalAdjuster(
                mutableListOf<Double>(2463.0, 6018.0, 6400.0, 6714.0),
                mutableListOf<Double>(0.96, 0.93, 0.96, 0.96),
            ),
            FlaggedSeasonalIdlingProposalAdjuster(flags, "seasonal-idling-proposal-adaptive", 7.8),
            MinimumSeasonalIdlingProposalAdjuster(3.3),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 47217.8,
    )
}
