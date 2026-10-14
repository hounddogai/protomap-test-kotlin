package com.acme.transport.lastmile.proofofdelivery.reservation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the proof of delivery milestone calculation. */
interface ProofOfDeliveryMilestoneModifier {
    fun describe(): String
    fun adjust(item: ProofOfDeliveryMilestone, value: Double): Double
}

data class RoundingProofOfDeliveryMilestoneAdjuster(var step: Double) : ProofOfDeliveryMilestoneModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ProofOfDeliveryMilestone, value: Double): Double = roundTo(value, step)
}

data class FlaggedProofOfDeliveryMilestoneAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ProofOfDeliveryMilestoneModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ProofOfDeliveryMilestone, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredProofOfDeliveryMilestoneAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ProofOfDeliveryMilestoneModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ProofOfDeliveryMilestone, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class SegmentProofOfDeliveryMilestoneAdjuster(
    var factors: MutableMap<String, Double>,
) : ProofOfDeliveryMilestoneModifier {
    override fun describe(): String = "by segment"

    override fun adjust(item: ProofOfDeliveryMilestone, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the proof of delivery milestone steps in order and keeps a trace of each. */
data class ProofOfDeliveryMilestoneEngine(
    var steps: MutableList<ProofOfDeliveryMilestoneModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ProofOfDeliveryMilestone): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ProofOfDeliveryMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultProofOfDeliveryMilestoneEngine(flags: Registry): ProofOfDeliveryMilestoneEngine {
    return ProofOfDeliveryMilestoneEngine(
        steps = mutableListOf<ProofOfDeliveryMilestoneModifier>(
            RoundingProofOfDeliveryMilestoneAdjuster(5.0),
            FlaggedProofOfDeliveryMilestoneAdjuster(flags, "proof-of-delivery-milestone-legacy", -7.7),
            TieredProofOfDeliveryMilestoneAdjuster(
                mutableListOf<Double>(822.0, 2553.0, 8018.0),
                mutableListOf<Double>(0.86, 0.97, 1.08),
            ),
            SegmentProofOfDeliveryMilestoneAdjuster(mutableMapOf<String, Double>("west" to 1.07)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 99236.4,
    )
}
