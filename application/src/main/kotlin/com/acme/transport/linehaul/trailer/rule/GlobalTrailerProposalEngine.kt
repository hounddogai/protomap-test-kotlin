package com.acme.transport.linehaul.trailer.rule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the global trailer proposal calculation. */
interface GlobalTrailerProposalStep {
    fun describe(): String
    fun adjust(item: GlobalTrailerProposal, value: Double): Double
}

data class MinimumGlobalTrailerProposalAdjuster(var minimum: Double) : GlobalTrailerProposalStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalTrailerProposal, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentGlobalTrailerProposalAdjuster(var percent: Double) : GlobalTrailerProposalStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalTrailerProposal, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class GlobalTrailerProposalSequenceOffsetAdjuster(var weight: Double) : GlobalTrailerProposalStep {
    override fun describe(): String = "plus sequence times $weight"

    override fun adjust(item: GlobalTrailerProposal, value: Double): Double {
        return value + item.sequence.toDouble() * weight
    }
}

data class FlaggedGlobalTrailerProposalAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalTrailerProposalStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalTrailerProposal, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class NameGlobalTrailerProposalAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalTrailerProposalStep {
    override fun describe(): String = "by name"

    override fun adjust(item: GlobalTrailerProposal, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the global trailer proposal steps in order and keeps a trace of each. */
data class GlobalTrailerProposalEngine(
    var steps: MutableList<GlobalTrailerProposalStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: GlobalTrailerProposal): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<GlobalTrailerProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultGlobalTrailerProposalEngine(flags: Registry): GlobalTrailerProposalEngine {
    return GlobalTrailerProposalEngine(
        steps = mutableListOf<GlobalTrailerProposalStep>(
            MinimumGlobalTrailerProposalAdjuster(38.7),
            PercentGlobalTrailerProposalAdjuster(-10.9),
            GlobalTrailerProposalSequenceOffsetAdjuster(1.57),
            FlaggedGlobalTrailerProposalAdjuster(flags, "global-trailer-proposal-scheduled", 9.2),
            NameGlobalTrailerProposalAdjuster(
                mutableMapOf<String, Double>("online" to 0.82, "north" to 0.73, "south" to 0.99),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 10718.4,
    )
}
