package com.acme.transport.lastmile.proofofdelivery.audit

import com.acme.transport.kit.numbers.roundTo

/** One step of the proof of delivery override calculation. */
interface ProofOfDeliveryOverrideAdjuster {
    fun describe(): String
    fun adjust(item: ProofOfDeliveryOverride, value: Double): Double
}

data class MinimumProofOfDeliveryOverrideAdjuster(var minimum: Double) : ProofOfDeliveryOverrideAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ProofOfDeliveryOverride, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingProofOfDeliveryOverrideAdjuster(var step: Double) : ProofOfDeliveryOverrideAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ProofOfDeliveryOverride, value: Double): Double = roundTo(value, step)
}

data class PercentProofOfDeliveryOverrideAdjuster(var percent: Double) : ProofOfDeliveryOverrideAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ProofOfDeliveryOverride, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class NameProofOfDeliveryOverrideAdjuster(
    var factors: MutableMap<String, Double>,
) : ProofOfDeliveryOverrideAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: ProofOfDeliveryOverride, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the proof of delivery override steps in order and keeps a trace of each. */
data class ProofOfDeliveryOverrideEngine(
    var steps: MutableList<ProofOfDeliveryOverrideAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ProofOfDeliveryOverride): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ProofOfDeliveryOverrideAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ProofOfDeliveryOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultProofOfDeliveryOverrideEngine(): ProofOfDeliveryOverrideEngine {
    return ProofOfDeliveryOverrideEngine(
        steps = mutableListOf<ProofOfDeliveryOverrideAdjuster>(
            MinimumProofOfDeliveryOverrideAdjuster(19.3),
            RoundingProofOfDeliveryOverrideAdjuster(0.1),
            PercentProofOfDeliveryOverrideAdjuster(-6.2),
            NameProofOfDeliveryOverrideAdjuster(
                mutableMapOf<String, Double>("online" to 0.96, "central" to 0.93),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.0,
        ceiling = 72653.7,
    )
}
