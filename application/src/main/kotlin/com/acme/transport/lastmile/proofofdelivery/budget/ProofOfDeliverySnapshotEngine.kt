package com.acme.transport.lastmile.proofofdelivery.budget

import com.acme.transport.kit.flags.Registry

/** One step of the proof of delivery snapshot calculation. */
interface ProofOfDeliverySnapshotAdjuster {
    fun describe(): String
    fun adjust(item: ProofOfDeliverySnapshot, value: Double): Double
}

data class PercentProofOfDeliverySnapshotAdjuster(var percent: Double) : ProofOfDeliverySnapshotAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ProofOfDeliverySnapshot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapProofOfDeliverySnapshotAdjuster(var cap: Double) : ProofOfDeliverySnapshotAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ProofOfDeliverySnapshot, value: Double): Double = minOf(value, cap)
}

data class FlaggedProofOfDeliverySnapshotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ProofOfDeliverySnapshotAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ProofOfDeliverySnapshot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the proof of delivery snapshot steps in order and keeps a trace of each. */
data class ProofOfDeliverySnapshotEngine(
    var steps: MutableList<ProofOfDeliverySnapshotAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ProofOfDeliverySnapshot): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ProofOfDeliverySnapshotAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ProofOfDeliverySnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultProofOfDeliverySnapshotEngine(flags: Registry): ProofOfDeliverySnapshotEngine {
    return ProofOfDeliverySnapshotEngine(
        steps = mutableListOf<ProofOfDeliverySnapshotAdjuster>(
            PercentProofOfDeliverySnapshotAdjuster(23.9),
            CapProofOfDeliverySnapshotAdjuster(21265.0),
            FlaggedProofOfDeliverySnapshotAdjuster(flags, "proof-of-delivery-snapshot-seasonal", 7.6),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 89343.0,
    )
}
