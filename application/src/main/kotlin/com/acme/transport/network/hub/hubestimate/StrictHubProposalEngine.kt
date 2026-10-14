package com.acme.transport.network.hub.hubestimate

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the strict hub proposal calculation. */
interface StrictHubProposalStep {
    fun describe(): String
    fun adjust(item: StrictHubProposal, value: Double): Double
}

data class RoundingStrictHubProposalAdjuster(var step: Double) : StrictHubProposalStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictHubProposal, value: Double): Double = roundTo(value, step)
}

data class FlaggedStrictHubProposalAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictHubProposalStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictHubProposal, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapStrictHubProposalAdjuster(var cap: Double) : StrictHubProposalStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictHubProposal, value: Double): Double = minOf(value, cap)
}

data class TieredStrictHubProposalAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictHubProposalStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictHubProposal, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class TitleStrictHubProposalAdjuster(var factors: MutableMap<String, Double>) : StrictHubProposalStep {
    override fun describe(): String = "by title"

    override fun adjust(item: StrictHubProposal, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentStrictHubProposalAdjuster(var percent: Double) : StrictHubProposalStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictHubProposal, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the strict hub proposal steps in order and keeps a trace of each. */
data class StrictHubProposalEngine(
    var steps: MutableList<StrictHubProposalStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: StrictHubProposal): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictHubProposalStep = steps[index]
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

    fun evaluateAll(items: MutableList<StrictHubProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultStrictHubProposalEngine(flags: Registry): StrictHubProposalEngine {
    return StrictHubProposalEngine(
        steps = mutableListOf<StrictHubProposalStep>(
            RoundingStrictHubProposalAdjuster(1.0),
            FlaggedStrictHubProposalAdjuster(flags, "strict-hub-proposal-scheduled", 6.9),
            CapStrictHubProposalAdjuster(44257.0),
            TieredStrictHubProposalAdjuster(
                mutableListOf<Double>(2171.0, 4920.0, 6864.0, 7393.0),
                mutableListOf<Double>(0.91, 0.99, 1.19, 1.0),
            ),
            TitleStrictHubProposalAdjuster(mutableMapOf<String, Double>("central" to 1.07)),
            PercentStrictHubProposalAdjuster(-21.2),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 18493.5,
    )
}
