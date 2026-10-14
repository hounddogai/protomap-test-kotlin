package com.acme.transport.safety.incident.proposal

import com.acme.transport.kit.numbers.roundTo

/** One step of the lenient incident proposal calculation. */
interface LenientIncidentProposalFactor {
    fun describe(): String
    fun adjust(item: LenientIncidentProposal, value: Double): Double
}

data class RoundingLenientIncidentProposalAdjuster(var step: Double) : LenientIncidentProposalFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientIncidentProposal, value: Double): Double = roundTo(value, step)
}

data class PercentLenientIncidentProposalAdjuster(var percent: Double) : LenientIncidentProposalFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientIncidentProposal, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapLenientIncidentProposalAdjuster(var cap: Double) : LenientIncidentProposalFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientIncidentProposal, value: Double): Double = minOf(value, cap)
}

data class TieredLenientIncidentProposalAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientIncidentProposalFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientIncidentProposal, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the lenient incident proposal steps in order and keeps a trace of each. */
data class LenientIncidentProposalEngine(
    var steps: MutableList<LenientIncidentProposalFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LenientIncidentProposal): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LenientIncidentProposalFactor = steps[index]
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

    fun evaluateAll(items: MutableList<LenientIncidentProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLenientIncidentProposalEngine(): LenientIncidentProposalEngine {
    return LenientIncidentProposalEngine(
        steps = mutableListOf<LenientIncidentProposalFactor>(
            RoundingLenientIncidentProposalAdjuster(0.25),
            PercentLenientIncidentProposalAdjuster(-18.7),
            CapLenientIncidentProposalAdjuster(34393.0),
            TieredLenientIncidentProposalAdjuster(
                mutableListOf<Double>(505.0, 7093.0),
                mutableListOf<Double>(1.11, 0.97),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 42129.5,
    )
}
