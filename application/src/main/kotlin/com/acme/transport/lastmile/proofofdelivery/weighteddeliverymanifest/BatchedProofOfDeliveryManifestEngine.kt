package com.acme.transport.lastmile.proofofdelivery.weighteddeliverymanifest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the batched proof of delivery manifest calculation. */
interface BatchedProofOfDeliveryManifestFactor {
    fun describe(): String
    fun adjust(item: BatchedProofOfDeliveryManifest, value: Double): Double
}

data class MinimumBatchedProofOfDeliveryManifestAdjuster(
    var minimum: Double,
) : BatchedProofOfDeliveryManifestFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedProofOfDeliveryManifest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedBatchedProofOfDeliveryManifestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedProofOfDeliveryManifestFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedProofOfDeliveryManifest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentBatchedProofOfDeliveryManifestAdjuster(
    var percent: Double,
) : BatchedProofOfDeliveryManifestFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedProofOfDeliveryManifest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingBatchedProofOfDeliveryManifestAdjuster(
    var step: Double,
) : BatchedProofOfDeliveryManifestFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedProofOfDeliveryManifest, value: Double): Double = roundTo(value, step)
}

data class CapBatchedProofOfDeliveryManifestAdjuster(var cap: Double) : BatchedProofOfDeliveryManifestFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedProofOfDeliveryManifest, value: Double): Double = minOf(value, cap)
}

/** Runs the batched proof of delivery manifest steps in order and keeps a trace of each. */
data class BatchedProofOfDeliveryManifestEngine(
    var steps: MutableList<BatchedProofOfDeliveryManifestFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BatchedProofOfDeliveryManifest): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedProofOfDeliveryManifestFactor = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedProofOfDeliveryManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBatchedProofOfDeliveryManifestEngine(flags: Registry): BatchedProofOfDeliveryManifestEngine {
    return BatchedProofOfDeliveryManifestEngine(
        steps = mutableListOf<BatchedProofOfDeliveryManifestFactor>(
            MinimumBatchedProofOfDeliveryManifestAdjuster(27.5),
            FlaggedBatchedProofOfDeliveryManifestAdjuster(
                flags,
                "batched-proof-of-delivery-manifest-capped",
                -7.5,
            ),
            PercentBatchedProofOfDeliveryManifestAdjuster(-21.3),
            RoundingBatchedProofOfDeliveryManifestAdjuster(1.0),
            CapBatchedProofOfDeliveryManifestAdjuster(29371.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 74497.3,
    )
}
