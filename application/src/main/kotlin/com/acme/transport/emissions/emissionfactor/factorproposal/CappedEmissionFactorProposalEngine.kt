package com.acme.transport.emissions.emissionfactor.factorproposal

import com.acme.transport.kit.numbers.roundTo

/** One step of the capped emission factor proposal calculation. */
interface CappedEmissionFactorProposalFactor {
    fun describe(): String
    fun adjust(item: CappedEmissionFactorProposal, value: Double): Double
}

data class TieredCappedEmissionFactorProposalAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedEmissionFactorProposalFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedEmissionFactorProposal, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CappedEmissionFactorProposalCeilingOffsetAdjuster(
    var weight: Double,
) : CappedEmissionFactorProposalFactor {
    override fun describe(): String = "plus ceiling times $weight"

    override fun adjust(item: CappedEmissionFactorProposal, value: Double): Double = value + item.ceiling * weight
}

data class MinimumCappedEmissionFactorProposalAdjuster(
    var minimum: Double,
) : CappedEmissionFactorProposalFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedEmissionFactorProposal, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingCappedEmissionFactorProposalAdjuster(var step: Double) : CappedEmissionFactorProposalFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedEmissionFactorProposal, value: Double): Double = roundTo(value, step)
}

data class CapCappedEmissionFactorProposalAdjuster(var cap: Double) : CappedEmissionFactorProposalFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedEmissionFactorProposal, value: Double): Double = minOf(value, cap)
}

/** Runs the capped emission factor proposal steps in order and keeps a trace of each. */
data class CappedEmissionFactorProposalEngine(
    var steps: MutableList<CappedEmissionFactorProposalFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CappedEmissionFactorProposal): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedEmissionFactorProposalFactor = steps[index]
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

    fun evaluateAll(items: MutableList<CappedEmissionFactorProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCappedEmissionFactorProposalEngine(): CappedEmissionFactorProposalEngine {
    return CappedEmissionFactorProposalEngine(
        steps = mutableListOf<CappedEmissionFactorProposalFactor>(
            TieredCappedEmissionFactorProposalAdjuster(
                mutableListOf<Double>(864.0, 6432.0, 9117.0),
                mutableListOf<Double>(0.99, 1.1, 0.81),
            ),
            CappedEmissionFactorProposalCeilingOffsetAdjuster(1.97),
            MinimumCappedEmissionFactorProposalAdjuster(49.5),
            RoundingCappedEmissionFactorProposalAdjuster(5.0),
            CapCappedEmissionFactorProposalAdjuster(29194.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 79953.3,
    )
}
