package com.acme.transport.lastmile.proofofdelivery.deliveryrollup

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the batched proof of delivery policy calculation. */
interface BatchedProofOfDeliveryPolicyFactor {
    fun describe(): String
    fun adjust(item: BatchedProofOfDeliveryPolicy, value: Double): Double
}

data class FlaggedBatchedProofOfDeliveryPolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedProofOfDeliveryPolicyFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedProofOfDeliveryPolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapBatchedProofOfDeliveryPolicyAdjuster(var cap: Double) : BatchedProofOfDeliveryPolicyFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedProofOfDeliveryPolicy, value: Double): Double = minOf(value, cap)
}

data class RoundingBatchedProofOfDeliveryPolicyAdjuster(var step: Double) : BatchedProofOfDeliveryPolicyFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedProofOfDeliveryPolicy, value: Double): Double = roundTo(value, step)
}

/** Runs the batched proof of delivery policy steps in order and keeps a trace of each. */
data class BatchedProofOfDeliveryPolicyEngine(
    var steps: MutableList<BatchedProofOfDeliveryPolicyFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: BatchedProofOfDeliveryPolicy): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedProofOfDeliveryPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultBatchedProofOfDeliveryPolicyEngine(flags: Registry): BatchedProofOfDeliveryPolicyEngine {
    return BatchedProofOfDeliveryPolicyEngine(
        steps = mutableListOf<BatchedProofOfDeliveryPolicyFactor>(
            FlaggedBatchedProofOfDeliveryPolicyAdjuster(
                flags,
                "batched-proof-of-delivery-policy-scheduled",
                9.5,
            ),
            CapBatchedProofOfDeliveryPolicyAdjuster(41714.0),
            RoundingBatchedProofOfDeliveryPolicyAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 24746.5,
    )
}
