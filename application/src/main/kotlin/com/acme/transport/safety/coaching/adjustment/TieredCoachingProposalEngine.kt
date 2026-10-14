package com.acme.transport.safety.coaching.adjustment

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the tiered coaching proposal calculation. */
interface TieredCoachingProposalStep {
    fun describe(): String
    fun adjust(item: TieredCoachingProposal, value: Double): Double
}

data class RoundingTieredCoachingProposalAdjuster(var step: Double) : TieredCoachingProposalStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredCoachingProposal, value: Double): Double = roundTo(value, step)
}

data class NameTieredCoachingProposalAdjuster(
    var factors: MutableMap<String, Double>,
) : TieredCoachingProposalStep {
    override fun describe(): String = "by name"

    override fun adjust(item: TieredCoachingProposal, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredTieredCoachingProposalAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredCoachingProposalStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredCoachingProposal, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class TieredCoachingProposalUpdatedAtOffsetAdjuster(var weight: Double) : TieredCoachingProposalStep {
    override fun describe(): String = "plus updated at times $weight"

    override fun adjust(item: TieredCoachingProposal, value: Double): Double {
        return value + item.updatedAt.toDouble() * weight
    }
}

data class MinimumTieredCoachingProposalAdjuster(var minimum: Double) : TieredCoachingProposalStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredCoachingProposal, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapTieredCoachingProposalAdjuster(var cap: Double) : TieredCoachingProposalStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredCoachingProposal, value: Double): Double = minOf(value, cap)
}

/** Runs the tiered coaching proposal steps in order and keeps a trace of each. */
data class TieredCoachingProposalEngine(
    var steps: MutableList<TieredCoachingProposalStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TieredCoachingProposal): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TieredCoachingProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTieredCoachingProposalEngine(): TieredCoachingProposalEngine {
    return TieredCoachingProposalEngine(
        steps = mutableListOf<TieredCoachingProposalStep>(
            RoundingTieredCoachingProposalAdjuster(0.05),
            NameTieredCoachingProposalAdjuster(mutableMapOf<String, Double>("north" to 1.17)),
            TieredTieredCoachingProposalAdjuster(
                mutableListOf<Double>(6011.0, 7749.0, 7891.0),
                mutableListOf<Double>(1.14, 0.88, 1.04),
            ),
            TieredCoachingProposalUpdatedAtOffsetAdjuster(0.83),
            MinimumTieredCoachingProposalAdjuster(7.8),
            CapTieredCoachingProposalAdjuster(30302.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 15687.2,
    )
}
