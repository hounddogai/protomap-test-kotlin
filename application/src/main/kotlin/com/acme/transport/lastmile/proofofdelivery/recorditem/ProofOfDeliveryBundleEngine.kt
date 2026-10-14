package com.acme.transport.lastmile.proofofdelivery.recorditem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the proof of delivery bundle calculation. */
interface ProofOfDeliveryBundleAdjuster {
    fun describe(): String
    fun adjust(item: ProofOfDeliveryBundle, value: Double): Double
}

data class FlaggedProofOfDeliveryBundleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ProofOfDeliveryBundleAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ProofOfDeliveryBundle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingProofOfDeliveryBundleAdjuster(var step: Double) : ProofOfDeliveryBundleAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ProofOfDeliveryBundle, value: Double): Double = roundTo(value, step)
}

data class MinimumProofOfDeliveryBundleAdjuster(var minimum: Double) : ProofOfDeliveryBundleAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ProofOfDeliveryBundle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapProofOfDeliveryBundleAdjuster(var cap: Double) : ProofOfDeliveryBundleAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ProofOfDeliveryBundle, value: Double): Double = minOf(value, cap)
}

/** Runs the proof of delivery bundle steps in order and keeps a trace of each. */
data class ProofOfDeliveryBundleEngine(
    var steps: MutableList<ProofOfDeliveryBundleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ProofOfDeliveryBundle): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ProofOfDeliveryBundleAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ProofOfDeliveryBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultProofOfDeliveryBundleEngine(flags: Registry): ProofOfDeliveryBundleEngine {
    return ProofOfDeliveryBundleEngine(
        steps = mutableListOf<ProofOfDeliveryBundleAdjuster>(
            FlaggedProofOfDeliveryBundleAdjuster(flags, "proof-of-delivery-bundle-primary", 6.9),
            RoundingProofOfDeliveryBundleAdjuster(0.5),
            MinimumProofOfDeliveryBundleAdjuster(47.0),
            CapProofOfDeliveryBundleAdjuster(38389.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 87613.3,
    )
}
