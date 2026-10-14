package com.acme.transport.lastmile.proofofdelivery.assignment

import com.acme.transport.kit.flags.Registry

/** One step of the proof of delivery manifest calculation. */
interface ProofOfDeliveryManifestStep {
    fun describe(): String
    fun adjust(item: ProofOfDeliveryManifest, value: Double): Double
}

data class PercentProofOfDeliveryManifestAdjuster(var percent: Double) : ProofOfDeliveryManifestStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ProofOfDeliveryManifest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LabelProofOfDeliveryManifestAdjuster(
    var factors: MutableMap<String, Double>,
) : ProofOfDeliveryManifestStep {
    override fun describe(): String = "by label"

    override fun adjust(item: ProofOfDeliveryManifest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedProofOfDeliveryManifestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ProofOfDeliveryManifestStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ProofOfDeliveryManifest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the proof of delivery manifest steps in order and keeps a trace of each. */
data class ProofOfDeliveryManifestEngine(
    var steps: MutableList<ProofOfDeliveryManifestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ProofOfDeliveryManifest): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ProofOfDeliveryManifestStep = steps[index]
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

    fun evaluateAll(items: MutableList<ProofOfDeliveryManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultProofOfDeliveryManifestEngine(flags: Registry): ProofOfDeliveryManifestEngine {
    return ProofOfDeliveryManifestEngine(
        steps = mutableListOf<ProofOfDeliveryManifestStep>(
            PercentProofOfDeliveryManifestAdjuster(8.6),
            LabelProofOfDeliveryManifestAdjuster(mutableMapOf<String, Double>("north" to 1.2, "east" to 0.76)),
            FlaggedProofOfDeliveryManifestAdjuster(flags, "proof-of-delivery-manifest-lenient", -6.4),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 5379.6,
    )
}
