package com.acme.transport.safety.scorecard.slot

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the default scorecard proposal calculation. */
interface DefaultScorecardProposalFactor {
    fun describe(): String
    fun adjust(item: DefaultScorecardProposal, value: Double): Double
}

data class PercentDefaultScorecardProposalAdjuster(var percent: Double) : DefaultScorecardProposalFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultScorecardProposal, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LocaleDefaultScorecardProposalAdjuster(
    var factors: MutableMap<String, Double>,
) : DefaultScorecardProposalFactor {
    override fun describe(): String = "by locale"

    override fun adjust(item: DefaultScorecardProposal, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredDefaultScorecardProposalAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultScorecardProposalFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultScorecardProposal, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapDefaultScorecardProposalAdjuster(var cap: Double) : DefaultScorecardProposalFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultScorecardProposal, value: Double): Double = minOf(value, cap)
}

/** Runs the default scorecard proposal steps in order and keeps a trace of each. */
data class DefaultScorecardProposalEngine(
    var steps: MutableList<DefaultScorecardProposalFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DefaultScorecardProposal): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DefaultScorecardProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDefaultScorecardProposalEngine(): DefaultScorecardProposalEngine {
    return DefaultScorecardProposalEngine(
        steps = mutableListOf<DefaultScorecardProposalFactor>(
            PercentDefaultScorecardProposalAdjuster(3.2),
            LocaleDefaultScorecardProposalAdjuster(
                mutableMapOf<String, Double>("central" to 0.77, "online" to 0.81),
            ),
            TieredDefaultScorecardProposalAdjuster(
                mutableListOf<Double>(1467.0, 5928.0, 8236.0, 9156.0),
                mutableListOf<Double>(1.14, 0.81, 0.82, 1.07),
            ),
            CapDefaultScorecardProposalAdjuster(19249.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 23693.6,
    )
}
