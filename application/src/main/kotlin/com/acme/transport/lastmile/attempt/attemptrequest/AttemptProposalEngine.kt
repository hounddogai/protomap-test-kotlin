package com.acme.transport.lastmile.attempt.attemptrequest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the attempt proposal calculation. */
interface AttemptProposalAdjuster {
    fun describe(): String
    fun adjust(item: AttemptProposal, value: Double): Double
}

data class PercentAttemptProposalAdjuster(var percent: Double) : AttemptProposalAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AttemptProposal, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class AttemptProposalAttemptsOffsetAdjuster(var weight: Double) : AttemptProposalAdjuster {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: AttemptProposal, value: Double): Double = value + item.attempts.toDouble() * weight
}

data class TieredAttemptProposalAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AttemptProposalAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AttemptProposal, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedAttemptProposalAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AttemptProposalAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AttemptProposal, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the attempt proposal steps in order and keeps a trace of each. */
data class AttemptProposalEngine(
    var steps: MutableList<AttemptProposalAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AttemptProposal): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AttemptProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAttemptProposalEngine(flags: Registry): AttemptProposalEngine {
    return AttemptProposalEngine(
        steps = mutableListOf<AttemptProposalAdjuster>(
            PercentAttemptProposalAdjuster(-9.2),
            AttemptProposalAttemptsOffsetAdjuster(0.53),
            TieredAttemptProposalAdjuster(
                mutableListOf<Double>(929.0, 5484.0),
                mutableListOf<Double>(0.84, 0.9),
            ),
            FlaggedAttemptProposalAdjuster(flags, "attempt-proposal-lenient", 7.2),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 17121.2,
    )
}
