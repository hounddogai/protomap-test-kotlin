package com.acme.transport.emissions.emissionfactor.proposal

import com.acme.transport.kit.numbers.roundTo

/** One step of the global emission factor proposal calculation. */
interface GlobalEmissionFactorProposalModifier {
    fun describe(): String
    fun adjust(item: GlobalEmissionFactorProposal, value: Double): Double
}

data class TieredGlobalEmissionFactorProposalAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalEmissionFactorProposalModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalEmissionFactorProposal, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapGlobalEmissionFactorProposalAdjuster(var cap: Double) : GlobalEmissionFactorProposalModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalEmissionFactorProposal, value: Double): Double = minOf(value, cap)
}

data class MinimumGlobalEmissionFactorProposalAdjuster(
    var minimum: Double,
) : GlobalEmissionFactorProposalModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalEmissionFactorProposal, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingGlobalEmissionFactorProposalAdjuster(
    var step: Double,
) : GlobalEmissionFactorProposalModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalEmissionFactorProposal, value: Double): Double = roundTo(value, step)
}

data class PercentGlobalEmissionFactorProposalAdjuster(
    var percent: Double,
) : GlobalEmissionFactorProposalModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalEmissionFactorProposal, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class GlobalEmissionFactorProposalDurationMinutesOffsetAdjuster(
    var weight: Double,
) : GlobalEmissionFactorProposalModifier {
    override fun describe(): String = "plus duration minutes times $weight"

    override fun adjust(item: GlobalEmissionFactorProposal, value: Double): Double {
        return value + item.durationMinutes.toDouble() * weight
    }
}

/** Runs the global emission factor proposal steps in order and keeps a trace of each. */
data class GlobalEmissionFactorProposalEngine(
    var steps: MutableList<GlobalEmissionFactorProposalModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: GlobalEmissionFactorProposal): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalEmissionFactorProposalModifier = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalEmissionFactorProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultGlobalEmissionFactorProposalEngine(): GlobalEmissionFactorProposalEngine {
    return GlobalEmissionFactorProposalEngine(
        steps = mutableListOf<GlobalEmissionFactorProposalModifier>(
            TieredGlobalEmissionFactorProposalAdjuster(
                mutableListOf<Double>(6818.0, 9121.0),
                mutableListOf<Double>(0.84, 0.99),
            ),
            CapGlobalEmissionFactorProposalAdjuster(6075.0),
            MinimumGlobalEmissionFactorProposalAdjuster(31.0),
            RoundingGlobalEmissionFactorProposalAdjuster(0.5),
            PercentGlobalEmissionFactorProposalAdjuster(1.4),
            GlobalEmissionFactorProposalDurationMinutesOffsetAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 59891.4,
    )
}
