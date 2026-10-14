package com.acme.transport.linehaul.capacity.budget

import com.acme.transport.kit.numbers.roundTo

/** One step of the lenient capacity proposal calculation. */
interface LenientCapacityProposalAdjuster {
    fun describe(): String
    fun adjust(item: LenientCapacityProposal, value: Double): Double
}

data class DepotLenientCapacityProposalAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientCapacityProposalAdjuster {
    override fun describe(): String = "by depot"

    override fun adjust(item: LenientCapacityProposal, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingLenientCapacityProposalAdjuster(var step: Double) : LenientCapacityProposalAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientCapacityProposal, value: Double): Double = roundTo(value, step)
}

data class LenientCapacityProposalCapacityOffsetAdjuster(var weight: Double) : LenientCapacityProposalAdjuster {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: LenientCapacityProposal, value: Double): Double {
        return value + item.capacity.toDouble() * weight
    }
}

data class MinimumLenientCapacityProposalAdjuster(var minimum: Double) : LenientCapacityProposalAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientCapacityProposal, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the lenient capacity proposal steps in order and keeps a trace of each. */
data class LenientCapacityProposalEngine(
    var steps: MutableList<LenientCapacityProposalAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LenientCapacityProposal): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LenientCapacityProposalAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<LenientCapacityProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLenientCapacityProposalEngine(): LenientCapacityProposalEngine {
    return LenientCapacityProposalEngine(
        steps = mutableListOf<LenientCapacityProposalAdjuster>(
            DepotLenientCapacityProposalAdjuster(
                mutableMapOf<String, Double>("retail" to 1.31, "south" to 1.39),
            ),
            RoundingLenientCapacityProposalAdjuster(0.25),
            LenientCapacityProposalCapacityOffsetAdjuster(0.25),
            MinimumLenientCapacityProposalAdjuster(5.7),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 80566.7,
    )
}
