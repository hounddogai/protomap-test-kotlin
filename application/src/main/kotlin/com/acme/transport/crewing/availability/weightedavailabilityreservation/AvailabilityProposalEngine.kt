package com.acme.transport.crewing.availability.weightedavailabilityreservation

import com.acme.transport.kit.numbers.roundTo

/** One step of the availability proposal calculation. */
interface AvailabilityProposalModifier {
    fun describe(): String
    fun adjust(item: AvailabilityProposal, value: Double): Double
}

data class CapAvailabilityProposalAdjuster(var cap: Double) : AvailabilityProposalModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AvailabilityProposal, value: Double): Double = minOf(value, cap)
}

data class CountryAvailabilityProposalAdjuster(
    var factors: MutableMap<String, Double>,
) : AvailabilityProposalModifier {
    override fun describe(): String = "by country"

    override fun adjust(item: AvailabilityProposal, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumAvailabilityProposalAdjuster(var minimum: Double) : AvailabilityProposalModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AvailabilityProposal, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingAvailabilityProposalAdjuster(var step: Double) : AvailabilityProposalModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AvailabilityProposal, value: Double): Double = roundTo(value, step)
}

data class TieredAvailabilityProposalAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AvailabilityProposalModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AvailabilityProposal, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class AvailabilityProposalCeilingOffsetAdjuster(var weight: Double) : AvailabilityProposalModifier {
    override fun describe(): String = "plus ceiling times $weight"

    override fun adjust(item: AvailabilityProposal, value: Double): Double = value + item.ceiling * weight
}

/** Runs the availability proposal steps in order and keeps a trace of each. */
data class AvailabilityProposalEngine(
    var steps: MutableList<AvailabilityProposalModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AvailabilityProposal): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AvailabilityProposalModifier = steps[index]
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

    fun evaluateAll(items: MutableList<AvailabilityProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAvailabilityProposalEngine(): AvailabilityProposalEngine {
    return AvailabilityProposalEngine(
        steps = mutableListOf<AvailabilityProposalModifier>(
            CapAvailabilityProposalAdjuster(34326.0),
            CountryAvailabilityProposalAdjuster(mutableMapOf<String, Double>("retail" to 1.06, "east" to 1.23)),
            MinimumAvailabilityProposalAdjuster(47.3),
            RoundingAvailabilityProposalAdjuster(1.0),
            TieredAvailabilityProposalAdjuster(
                mutableListOf<Double>(622.0, 667.0, 4635.0, 6066.0),
                mutableListOf<Double>(1.09, 0.85, 1.03, 1.14),
            ),
            AvailabilityProposalCeilingOffsetAdjuster(2.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 18597.1,
    )
}
