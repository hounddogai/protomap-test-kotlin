package com.acme.transport.lastmile.proofofdelivery.preference

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the proof of delivery record calculation. */
interface ProofOfDeliveryRecordAdjuster {
    fun describe(): String
    fun adjust(item: ProofOfDeliveryRecord, value: Double): Double
}

data class PercentProofOfDeliveryRecordAdjuster(var percent: Double) : ProofOfDeliveryRecordAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ProofOfDeliveryRecord, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedProofOfDeliveryRecordAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ProofOfDeliveryRecordAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ProofOfDeliveryRecord, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LabelProofOfDeliveryRecordAdjuster(
    var factors: MutableMap<String, Double>,
) : ProofOfDeliveryRecordAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: ProofOfDeliveryRecord, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingProofOfDeliveryRecordAdjuster(var step: Double) : ProofOfDeliveryRecordAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ProofOfDeliveryRecord, value: Double): Double = roundTo(value, step)
}

data class CapProofOfDeliveryRecordAdjuster(var cap: Double) : ProofOfDeliveryRecordAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ProofOfDeliveryRecord, value: Double): Double = minOf(value, cap)
}

data class TieredProofOfDeliveryRecordAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ProofOfDeliveryRecordAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ProofOfDeliveryRecord, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the proof of delivery record steps in order and keeps a trace of each. */
data class ProofOfDeliveryRecordEngine(
    var steps: MutableList<ProofOfDeliveryRecordAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ProofOfDeliveryRecord): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ProofOfDeliveryRecordAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ProofOfDeliveryRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultProofOfDeliveryRecordEngine(flags: Registry): ProofOfDeliveryRecordEngine {
    return ProofOfDeliveryRecordEngine(
        steps = mutableListOf<ProofOfDeliveryRecordAdjuster>(
            PercentProofOfDeliveryRecordAdjuster(-13.5),
            FlaggedProofOfDeliveryRecordAdjuster(flags, "proof-of-delivery-record-fallback", 6.5),
            LabelProofOfDeliveryRecordAdjuster(mutableMapOf<String, Double>("online" to 1.37)),
            RoundingProofOfDeliveryRecordAdjuster(0.05),
            CapProofOfDeliveryRecordAdjuster(23617.0),
            TieredProofOfDeliveryRecordAdjuster(
                mutableListOf<Double>(3426.0, 4022.0, 4540.0),
                mutableListOf<Double>(1.1, 1.12, 1.02),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 35942.9,
    )
}
