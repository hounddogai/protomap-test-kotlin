package com.acme.transport.crewing.qualification.qualificationproposal

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the adaptive qualification proposal calculation. */
interface AdaptiveQualificationProposalAdjuster {
    fun describe(): String
    fun adjust(item: AdaptiveQualificationProposal, value: Double): Double
}

data class MinimumAdaptiveQualificationProposalAdjuster(
    var minimum: Double,
) : AdaptiveQualificationProposalAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveQualificationProposal, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class AdaptiveQualificationProposalAmountOffsetAdjuster(
    var weight: Double,
) : AdaptiveQualificationProposalAdjuster {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: AdaptiveQualificationProposal, value: Double): Double = value + item.amount * weight
}

data class RoundingAdaptiveQualificationProposalAdjuster(
    var step: Double,
) : AdaptiveQualificationProposalAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveQualificationProposal, value: Double): Double = roundTo(value, step)
}

data class TieredAdaptiveQualificationProposalAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveQualificationProposalAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveQualificationProposal, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapAdaptiveQualificationProposalAdjuster(var cap: Double) : AdaptiveQualificationProposalAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveQualificationProposal, value: Double): Double = minOf(value, cap)
}

data class SegmentAdaptiveQualificationProposalAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveQualificationProposalAdjuster {
    override fun describe(): String = "by segment"

    override fun adjust(item: AdaptiveQualificationProposal, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the adaptive qualification proposal steps in order and keeps a trace of each. */
data class AdaptiveQualificationProposalEngine(
    var steps: MutableList<AdaptiveQualificationProposalAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AdaptiveQualificationProposal): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AdaptiveQualificationProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAdaptiveQualificationProposalEngine(): AdaptiveQualificationProposalEngine {
    return AdaptiveQualificationProposalEngine(
        steps = mutableListOf<AdaptiveQualificationProposalAdjuster>(
            MinimumAdaptiveQualificationProposalAdjuster(49.6),
            AdaptiveQualificationProposalAmountOffsetAdjuster(0.7),
            RoundingAdaptiveQualificationProposalAdjuster(0.5),
            TieredAdaptiveQualificationProposalAdjuster(
                mutableListOf<Double>(3276.0, 5195.0, 6582.0, 9339.0),
                mutableListOf<Double>(0.86, 0.98, 1.19, 1.1),
            ),
            CapAdaptiveQualificationProposalAdjuster(43508.0),
            SegmentAdaptiveQualificationProposalAdjuster(
                mutableMapOf<String, Double>("retail" to 1.25, "central" to 1.15),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 3527.9,
    )
}
