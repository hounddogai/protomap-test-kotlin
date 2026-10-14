package com.acme.transport.linehaul.backhaul.request

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the adaptive backhaul proposal calculation. */
interface AdaptiveBackhaulProposalModifier {
    fun describe(): String
    fun adjust(item: AdaptiveBackhaulProposal, value: Double): Double
}

data class PercentAdaptiveBackhaulProposalAdjuster(var percent: Double) : AdaptiveBackhaulProposalModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveBackhaulProposal, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapAdaptiveBackhaulProposalAdjuster(var cap: Double) : AdaptiveBackhaulProposalModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveBackhaulProposal, value: Double): Double = minOf(value, cap)
}

data class AdaptiveBackhaulProposalPriorityOffsetAdjuster(
    var weight: Double,
) : AdaptiveBackhaulProposalModifier {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: AdaptiveBackhaulProposal, value: Double): Double {
        return value + item.priority.toDouble() * weight
    }
}

data class RoundingAdaptiveBackhaulProposalAdjuster(var step: Double) : AdaptiveBackhaulProposalModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveBackhaulProposal, value: Double): Double = roundTo(value, step)
}

data class MinimumAdaptiveBackhaulProposalAdjuster(var minimum: Double) : AdaptiveBackhaulProposalModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveBackhaulProposal, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredAdaptiveBackhaulProposalAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveBackhaulProposalModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveBackhaulProposal, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the adaptive backhaul proposal steps in order and keeps a trace of each. */
data class AdaptiveBackhaulProposalEngine(
    var steps: MutableList<AdaptiveBackhaulProposalModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AdaptiveBackhaulProposal): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AdaptiveBackhaulProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAdaptiveBackhaulProposalEngine(): AdaptiveBackhaulProposalEngine {
    return AdaptiveBackhaulProposalEngine(
        steps = mutableListOf<AdaptiveBackhaulProposalModifier>(
            PercentAdaptiveBackhaulProposalAdjuster(-6.9),
            CapAdaptiveBackhaulProposalAdjuster(18499.0),
            AdaptiveBackhaulProposalPriorityOffsetAdjuster(1.36),
            RoundingAdaptiveBackhaulProposalAdjuster(0.05),
            MinimumAdaptiveBackhaulProposalAdjuster(2.0),
            TieredAdaptiveBackhaulProposalAdjuster(
                mutableListOf<Double>(4158.0, 4384.0, 6292.0, 7522.0),
                mutableListOf<Double>(1.01, 1.07, 1.09, 1.16),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 28549.3,
    )
}
