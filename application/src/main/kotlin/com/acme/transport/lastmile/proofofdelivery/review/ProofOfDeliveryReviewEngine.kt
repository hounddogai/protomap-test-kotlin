package com.acme.transport.lastmile.proofofdelivery.review

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the proof of delivery review calculation. */
interface ProofOfDeliveryReviewModifier {
    fun describe(): String
    fun adjust(item: ProofOfDeliveryReview, value: Double): Double
}

data class MinimumProofOfDeliveryReviewAdjuster(var minimum: Double) : ProofOfDeliveryReviewModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ProofOfDeliveryReview, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class NameProofOfDeliveryReviewAdjuster(
    var factors: MutableMap<String, Double>,
) : ProofOfDeliveryReviewModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: ProofOfDeliveryReview, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingProofOfDeliveryReviewAdjuster(var step: Double) : ProofOfDeliveryReviewModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ProofOfDeliveryReview, value: Double): Double = roundTo(value, step)
}

/** Runs the proof of delivery review steps in order and keeps a trace of each. */
data class ProofOfDeliveryReviewEngine(
    var steps: MutableList<ProofOfDeliveryReviewModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ProofOfDeliveryReview): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ProofOfDeliveryReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultProofOfDeliveryReviewEngine(): ProofOfDeliveryReviewEngine {
    return ProofOfDeliveryReviewEngine(
        steps = mutableListOf<ProofOfDeliveryReviewModifier>(
            MinimumProofOfDeliveryReviewAdjuster(0.3),
            NameProofOfDeliveryReviewAdjuster(mutableMapOf<String, Double>("north" to 1.19)),
            RoundingProofOfDeliveryReviewAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 12215.0,
    )
}
