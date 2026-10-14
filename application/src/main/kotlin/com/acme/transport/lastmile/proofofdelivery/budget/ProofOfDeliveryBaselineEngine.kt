package com.acme.transport.lastmile.proofofdelivery.budget

import com.acme.transport.kit.flags.Registry

/** One step of the proof of delivery baseline calculation. */
interface ProofOfDeliveryBaselineFactor {
    fun describe(): String
    fun adjust(item: ProofOfDeliveryBaseline, value: Double): Double
}

data class TieredProofOfDeliveryBaselineAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ProofOfDeliveryBaselineFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ProofOfDeliveryBaseline, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentProofOfDeliveryBaselineAdjuster(var percent: Double) : ProofOfDeliveryBaselineFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ProofOfDeliveryBaseline, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapProofOfDeliveryBaselineAdjuster(var cap: Double) : ProofOfDeliveryBaselineFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ProofOfDeliveryBaseline, value: Double): Double = minOf(value, cap)
}

data class FlaggedProofOfDeliveryBaselineAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ProofOfDeliveryBaselineFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ProofOfDeliveryBaseline, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumProofOfDeliveryBaselineAdjuster(var minimum: Double) : ProofOfDeliveryBaselineFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ProofOfDeliveryBaseline, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LabelProofOfDeliveryBaselineAdjuster(
    var factors: MutableMap<String, Double>,
) : ProofOfDeliveryBaselineFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: ProofOfDeliveryBaseline, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the proof of delivery baseline steps in order and keeps a trace of each. */
data class ProofOfDeliveryBaselineEngine(
    var steps: MutableList<ProofOfDeliveryBaselineFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ProofOfDeliveryBaseline): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ProofOfDeliveryBaselineFactor = steps[index]
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

    fun evaluateAll(items: MutableList<ProofOfDeliveryBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultProofOfDeliveryBaselineEngine(flags: Registry): ProofOfDeliveryBaselineEngine {
    return ProofOfDeliveryBaselineEngine(
        steps = mutableListOf<ProofOfDeliveryBaselineFactor>(
            TieredProofOfDeliveryBaselineAdjuster(
                mutableListOf<Double>(21.0, 546.0, 8154.0, 8702.0),
                mutableListOf<Double>(1.1, 1.19, 0.94, 1.12),
            ),
            PercentProofOfDeliveryBaselineAdjuster(1.9),
            CapProofOfDeliveryBaselineAdjuster(40580.0),
            FlaggedProofOfDeliveryBaselineAdjuster(flags, "proof-of-delivery-baseline-pending", 2.1),
            MinimumProofOfDeliveryBaselineAdjuster(48.9),
            LabelProofOfDeliveryBaselineAdjuster(
                mutableMapOf<String, Double>("south" to 1.05, "online" to 1.18),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 41413.3,
    )
}
