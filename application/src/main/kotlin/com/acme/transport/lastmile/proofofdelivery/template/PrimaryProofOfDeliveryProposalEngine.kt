package com.acme.transport.lastmile.proofofdelivery.template

import com.acme.transport.kit.numbers.roundTo

/** One step of the primary proof of delivery proposal calculation. */
interface PrimaryProofOfDeliveryProposalModifier {
    fun describe(): String
    fun adjust(item: PrimaryProofOfDeliveryProposal, value: Double): Double
}

data class MinimumPrimaryProofOfDeliveryProposalAdjuster(
    var minimum: Double,
) : PrimaryProofOfDeliveryProposalModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryProofOfDeliveryProposal, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PrimaryProofOfDeliveryProposalAttemptsOffsetAdjuster(
    var weight: Double,
) : PrimaryProofOfDeliveryProposalModifier {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: PrimaryProofOfDeliveryProposal, value: Double): Double {
        return value + item.attempts.toDouble() * weight
    }
}

data class TitlePrimaryProofOfDeliveryProposalAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryProofOfDeliveryProposalModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: PrimaryProofOfDeliveryProposal, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentPrimaryProofOfDeliveryProposalAdjuster(
    var percent: Double,
) : PrimaryProofOfDeliveryProposalModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryProofOfDeliveryProposal, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingPrimaryProofOfDeliveryProposalAdjuster(
    var step: Double,
) : PrimaryProofOfDeliveryProposalModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryProofOfDeliveryProposal, value: Double): Double = roundTo(value, step)
}

/** Runs the primary proof of delivery proposal steps in order and keeps a trace of each. */
data class PrimaryProofOfDeliveryProposalEngine(
    var steps: MutableList<PrimaryProofOfDeliveryProposalModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PrimaryProofOfDeliveryProposal): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryProofOfDeliveryProposalModifier = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryProofOfDeliveryProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPrimaryProofOfDeliveryProposalEngine(): PrimaryProofOfDeliveryProposalEngine {
    return PrimaryProofOfDeliveryProposalEngine(
        steps = mutableListOf<PrimaryProofOfDeliveryProposalModifier>(
            MinimumPrimaryProofOfDeliveryProposalAdjuster(22.6),
            PrimaryProofOfDeliveryProposalAttemptsOffsetAdjuster(0.66),
            TitlePrimaryProofOfDeliveryProposalAdjuster(
                mutableMapOf<String, Double>("south" to 0.74, "east" to 1.31, "online" to 1.18),
            ),
            PercentPrimaryProofOfDeliveryProposalAdjuster(-8.0),
            RoundingPrimaryProofOfDeliveryProposalAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 53352.3,
    )
}
