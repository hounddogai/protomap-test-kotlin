package com.acme.transport.linehaul.relay.assignment

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the relay proposal calculation. */
interface RelayProposalAdjuster {
    fun describe(): String
    fun adjust(item: RelayProposal, value: Double): Double
}

data class MinimumRelayProposalAdjuster(var minimum: Double) : RelayProposalAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RelayProposal, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingRelayProposalAdjuster(var step: Double) : RelayProposalAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RelayProposal, value: Double): Double = roundTo(value, step)
}

data class RegionRelayProposalAdjuster(var factors: MutableMap<String, Double>) : RelayProposalAdjuster {
    override fun describe(): String = "by region"

    override fun adjust(item: RelayProposal, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredRelayProposalAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RelayProposalAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RelayProposal, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the relay proposal steps in order and keeps a trace of each. */
data class RelayProposalEngine(
    var steps: MutableList<RelayProposalAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RelayProposal): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RelayProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRelayProposalEngine(): RelayProposalEngine {
    return RelayProposalEngine(
        steps = mutableListOf<RelayProposalAdjuster>(
            MinimumRelayProposalAdjuster(34.7),
            RoundingRelayProposalAdjuster(0.5),
            RegionRelayProposalAdjuster(mutableMapOf<String, Double>("east" to 0.87)),
            TieredRelayProposalAdjuster(
                mutableListOf<Double>(2517.0, 8951.0),
                mutableListOf<Double>(0.99, 0.87),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 96899.4,
    )
}
