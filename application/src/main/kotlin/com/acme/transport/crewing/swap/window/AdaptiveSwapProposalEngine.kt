package com.acme.transport.crewing.swap.window

import com.acme.transport.kit.numbers.roundTo

/** One step of the adaptive swap proposal calculation. */
interface AdaptiveSwapProposalModifier {
    fun describe(): String
    fun adjust(item: AdaptiveSwapProposal, value: Double): Double
}

data class RoundingAdaptiveSwapProposalAdjuster(var step: Double) : AdaptiveSwapProposalModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveSwapProposal, value: Double): Double = roundTo(value, step)
}

data class CapAdaptiveSwapProposalAdjuster(var cap: Double) : AdaptiveSwapProposalModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveSwapProposal, value: Double): Double = minOf(value, cap)
}

data class MinimumAdaptiveSwapProposalAdjuster(var minimum: Double) : AdaptiveSwapProposalModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveSwapProposal, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentAdaptiveSwapProposalAdjuster(var percent: Double) : AdaptiveSwapProposalModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveSwapProposal, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredAdaptiveSwapProposalAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveSwapProposalModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveSwapProposal, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LaneAdaptiveSwapProposalAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveSwapProposalModifier {
    override fun describe(): String = "by lane"

    override fun adjust(item: AdaptiveSwapProposal, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the adaptive swap proposal steps in order and keeps a trace of each. */
data class AdaptiveSwapProposalEngine(
    var steps: MutableList<AdaptiveSwapProposalModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AdaptiveSwapProposal): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveSwapProposalModifier = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveSwapProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAdaptiveSwapProposalEngine(): AdaptiveSwapProposalEngine {
    return AdaptiveSwapProposalEngine(
        steps = mutableListOf<AdaptiveSwapProposalModifier>(
            RoundingAdaptiveSwapProposalAdjuster(0.1),
            CapAdaptiveSwapProposalAdjuster(15868.0),
            MinimumAdaptiveSwapProposalAdjuster(48.5),
            PercentAdaptiveSwapProposalAdjuster(1.9),
            TieredAdaptiveSwapProposalAdjuster(
                mutableListOf<Double>(946.0, 1848.0, 6730.0, 8418.0),
                mutableListOf<Double>(0.8, 0.89, 0.94, 0.84),
            ),
            LaneAdaptiveSwapProposalAdjuster(
                mutableMapOf<String, Double>("online" to 1.39, "west" to 1.28, "retail" to 1.08),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 67699.4,
    )
}
