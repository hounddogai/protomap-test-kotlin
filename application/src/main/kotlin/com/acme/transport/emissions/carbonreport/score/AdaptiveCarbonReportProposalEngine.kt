package com.acme.transport.emissions.carbonreport.score

import com.acme.transport.kit.numbers.roundTo

/** One step of the adaptive carbon report proposal calculation. */
interface AdaptiveCarbonReportProposalFactor {
    fun describe(): String
    fun adjust(item: AdaptiveCarbonReportProposal, value: Double): Double
}

data class AdaptiveCarbonReportProposalStartsAtOffsetAdjuster(
    var weight: Double,
) : AdaptiveCarbonReportProposalFactor {
    override fun describe(): String = "plus starts at times $weight"

    override fun adjust(item: AdaptiveCarbonReportProposal, value: Double): Double {
        return value + item.startsAt.toDouble() * weight
    }
}

data class LaneAdaptiveCarbonReportProposalAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveCarbonReportProposalFactor {
    override fun describe(): String = "by lane"

    override fun adjust(item: AdaptiveCarbonReportProposal, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapAdaptiveCarbonReportProposalAdjuster(var cap: Double) : AdaptiveCarbonReportProposalFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveCarbonReportProposal, value: Double): Double = minOf(value, cap)
}

data class RoundingAdaptiveCarbonReportProposalAdjuster(var step: Double) : AdaptiveCarbonReportProposalFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveCarbonReportProposal, value: Double): Double = roundTo(value, step)
}

/** Runs the adaptive carbon report proposal steps in order and keeps a trace of each. */
data class AdaptiveCarbonReportProposalEngine(
    var steps: MutableList<AdaptiveCarbonReportProposalFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AdaptiveCarbonReportProposal): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveCarbonReportProposalFactor = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveCarbonReportProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAdaptiveCarbonReportProposalEngine(): AdaptiveCarbonReportProposalEngine {
    return AdaptiveCarbonReportProposalEngine(
        steps = mutableListOf<AdaptiveCarbonReportProposalFactor>(
            AdaptiveCarbonReportProposalStartsAtOffsetAdjuster(1.76),
            LaneAdaptiveCarbonReportProposalAdjuster(
                mutableMapOf<String, Double>("west" to 1.12, "south" to 0.98),
            ),
            CapAdaptiveCarbonReportProposalAdjuster(18037.0),
            RoundingAdaptiveCarbonReportProposalAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 56685.3,
    )
}
