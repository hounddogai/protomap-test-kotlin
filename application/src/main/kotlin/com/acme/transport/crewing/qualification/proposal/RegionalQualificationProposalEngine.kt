package com.acme.transport.crewing.qualification.proposal

import com.acme.transport.kit.numbers.roundTo

/** One step of the regional qualification proposal calculation. */
interface RegionalQualificationProposalAdjuster {
    fun describe(): String
    fun adjust(item: RegionalQualificationProposal, value: Double): Double
}

data class CapRegionalQualificationProposalAdjuster(var cap: Double) : RegionalQualificationProposalAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalQualificationProposal, value: Double): Double = minOf(value, cap)
}

data class MinimumRegionalQualificationProposalAdjuster(
    var minimum: Double,
) : RegionalQualificationProposalAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalQualificationProposal, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RegionalQualificationProposalFloorValueOffsetAdjuster(
    var weight: Double,
) : RegionalQualificationProposalAdjuster {
    override fun describe(): String = "plus floor value times $weight"

    override fun adjust(item: RegionalQualificationProposal, value: Double): Double = value + item.floorValue * weight
}

data class TitleRegionalQualificationProposalAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalQualificationProposalAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: RegionalQualificationProposal, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingRegionalQualificationProposalAdjuster(
    var step: Double,
) : RegionalQualificationProposalAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalQualificationProposal, value: Double): Double = roundTo(value, step)
}

/** Runs the regional qualification proposal steps in order and keeps a trace of each. */
data class RegionalQualificationProposalEngine(
    var steps: MutableList<RegionalQualificationProposalAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RegionalQualificationProposal): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalQualificationProposalAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalQualificationProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRegionalQualificationProposalEngine(): RegionalQualificationProposalEngine {
    return RegionalQualificationProposalEngine(
        steps = mutableListOf<RegionalQualificationProposalAdjuster>(
            CapRegionalQualificationProposalAdjuster(32444.0),
            MinimumRegionalQualificationProposalAdjuster(20.3),
            RegionalQualificationProposalFloorValueOffsetAdjuster(0.25),
            TitleRegionalQualificationProposalAdjuster(mutableMapOf<String, Double>("retail" to 1.27)),
            RoundingRegionalQualificationProposalAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 0.0,
        ceiling = 92471.3,
    )
}
