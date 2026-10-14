package com.acme.transport.lastmile.proofofdelivery.rollup

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the batched proof of delivery rollup calculation. */
interface BatchedProofOfDeliveryRollupStep {
    fun describe(): String
    fun adjust(item: BatchedProofOfDeliveryRollup, value: Double): Double
}

data class MinimumBatchedProofOfDeliveryRollupAdjuster(var minimum: Double) : BatchedProofOfDeliveryRollupStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedProofOfDeliveryRollup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedBatchedProofOfDeliveryRollupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedProofOfDeliveryRollupStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedProofOfDeliveryRollup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class BatchedProofOfDeliveryRollupDurationMinutesOffsetAdjuster(
    var weight: Double,
) : BatchedProofOfDeliveryRollupStep {
    override fun describe(): String = "plus duration minutes times $weight"

    override fun adjust(item: BatchedProofOfDeliveryRollup, value: Double): Double {
        return value + item.durationMinutes.toDouble() * weight
    }
}

data class CapBatchedProofOfDeliveryRollupAdjuster(var cap: Double) : BatchedProofOfDeliveryRollupStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedProofOfDeliveryRollup, value: Double): Double = minOf(value, cap)
}

data class RoundingBatchedProofOfDeliveryRollupAdjuster(var step: Double) : BatchedProofOfDeliveryRollupStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedProofOfDeliveryRollup, value: Double): Double = roundTo(value, step)
}

data class PercentBatchedProofOfDeliveryRollupAdjuster(var percent: Double) : BatchedProofOfDeliveryRollupStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedProofOfDeliveryRollup, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the batched proof of delivery rollup steps in order and keeps a trace of each. */
data class BatchedProofOfDeliveryRollupEngine(
    var steps: MutableList<BatchedProofOfDeliveryRollupStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: BatchedProofOfDeliveryRollup): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedProofOfDeliveryRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultBatchedProofOfDeliveryRollupEngine(flags: Registry): BatchedProofOfDeliveryRollupEngine {
    return BatchedProofOfDeliveryRollupEngine(
        steps = mutableListOf<BatchedProofOfDeliveryRollupStep>(
            MinimumBatchedProofOfDeliveryRollupAdjuster(30.8),
            FlaggedBatchedProofOfDeliveryRollupAdjuster(flags, "batched-proof-of-delivery-rollup-lenient", 9.6),
            BatchedProofOfDeliveryRollupDurationMinutesOffsetAdjuster(1.97),
            CapBatchedProofOfDeliveryRollupAdjuster(45310.0),
            RoundingBatchedProofOfDeliveryRollupAdjuster(0.25),
            PercentBatchedProofOfDeliveryRollupAdjuster(19.8),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 39958.1,
    )
}
