package com.acme.transport.lastmile.proofofdelivery.mapping

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the proof of delivery digest calculation. */
interface ProofOfDeliveryDigestAdjuster {
    fun describe(): String
    fun adjust(item: ProofOfDeliveryDigest, value: Double): Double
}

data class ProofOfDeliveryDigestDistanceKmOffsetAdjuster(var weight: Double) : ProofOfDeliveryDigestAdjuster {
    override fun describe(): String = "plus distance km times $weight"

    override fun adjust(item: ProofOfDeliveryDigest, value: Double): Double = value + item.distanceKm * weight
}

data class FlaggedProofOfDeliveryDigestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ProofOfDeliveryDigestAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ProofOfDeliveryDigest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingProofOfDeliveryDigestAdjuster(var step: Double) : ProofOfDeliveryDigestAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ProofOfDeliveryDigest, value: Double): Double = roundTo(value, step)
}

/** Runs the proof of delivery digest steps in order and keeps a trace of each. */
data class ProofOfDeliveryDigestEngine(
    var steps: MutableList<ProofOfDeliveryDigestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ProofOfDeliveryDigest): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ProofOfDeliveryDigestAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ProofOfDeliveryDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultProofOfDeliveryDigestEngine(flags: Registry): ProofOfDeliveryDigestEngine {
    return ProofOfDeliveryDigestEngine(
        steps = mutableListOf<ProofOfDeliveryDigestAdjuster>(
            ProofOfDeliveryDigestDistanceKmOffsetAdjuster(0.23),
            FlaggedProofOfDeliveryDigestAdjuster(flags, "proof-of-delivery-digest-legacy", -0.6),
            RoundingProofOfDeliveryDigestAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 17412.8,
    )
}
