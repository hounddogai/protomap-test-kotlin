package com.acme.transport.safety.coaching.digest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the coaching proposal calculation. */
interface CoachingProposalAdjuster {
    fun describe(): String
    fun adjust(item: CoachingProposal, value: Double): Double
}

data class TieredCoachingProposalAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CoachingProposalAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CoachingProposal, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentCoachingProposalAdjuster(var percent: Double) : CoachingProposalAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CoachingProposal, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class UnitCoachingProposalAdjuster(var factors: MutableMap<String, Double>) : CoachingProposalAdjuster {
    override fun describe(): String = "by unit"

    override fun adjust(item: CoachingProposal, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedCoachingProposalAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CoachingProposalAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CoachingProposal, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumCoachingProposalAdjuster(var minimum: Double) : CoachingProposalAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CoachingProposal, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingCoachingProposalAdjuster(var step: Double) : CoachingProposalAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CoachingProposal, value: Double): Double = roundTo(value, step)
}

/** Runs the coaching proposal steps in order and keeps a trace of each. */
data class CoachingProposalEngine(
    var steps: MutableList<CoachingProposalAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CoachingProposal): Double {
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

    fun evaluateAll(items: MutableList<CoachingProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCoachingProposalEngine(flags: Registry): CoachingProposalEngine {
    return CoachingProposalEngine(
        steps = mutableListOf<CoachingProposalAdjuster>(
            TieredCoachingProposalAdjuster(
                mutableListOf<Double>(2010.0, 4735.0),
                mutableListOf<Double>(1.19, 1.07),
            ),
            PercentCoachingProposalAdjuster(-2.0),
            UnitCoachingProposalAdjuster(
                mutableMapOf<String, Double>("retail" to 1.22, "online" to 1.36, "south" to 0.73),
            ),
            FlaggedCoachingProposalAdjuster(flags, "coaching-proposal-regional", -2.9),
            MinimumCoachingProposalAdjuster(38.6),
            RoundingCoachingProposalAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 75679.7,
    )
}
