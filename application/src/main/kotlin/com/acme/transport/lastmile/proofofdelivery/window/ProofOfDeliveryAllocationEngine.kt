package com.acme.transport.lastmile.proofofdelivery.window

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the proof of delivery allocation calculation. */
interface ProofOfDeliveryAllocationAdjuster {
    fun describe(): String
    fun adjust(item: ProofOfDeliveryAllocation, value: Double): Double
}

data class ChannelProofOfDeliveryAllocationAdjuster(
    var factors: MutableMap<String, Double>,
) : ProofOfDeliveryAllocationAdjuster {
    override fun describe(): String = "by channel"

    override fun adjust(item: ProofOfDeliveryAllocation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedProofOfDeliveryAllocationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ProofOfDeliveryAllocationAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ProofOfDeliveryAllocation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class ProofOfDeliveryAllocationRateOffsetAdjuster(var weight: Double) : ProofOfDeliveryAllocationAdjuster {
    override fun describe(): String = "plus rate times $weight"

    override fun adjust(item: ProofOfDeliveryAllocation, value: Double): Double = value + item.rate * weight
}

/** Runs the proof of delivery allocation steps in order and keeps a trace of each. */
data class ProofOfDeliveryAllocationEngine(
    var steps: MutableList<ProofOfDeliveryAllocationAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ProofOfDeliveryAllocation): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ProofOfDeliveryAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultProofOfDeliveryAllocationEngine(flags: Registry): ProofOfDeliveryAllocationEngine {
    return ProofOfDeliveryAllocationEngine(
        steps = mutableListOf<ProofOfDeliveryAllocationAdjuster>(
            ChannelProofOfDeliveryAllocationAdjuster(mutableMapOf<String, Double>("retail" to 1.11)),
            FlaggedProofOfDeliveryAllocationAdjuster(flags, "proof-of-delivery-allocation-batched", -5.1),
            ProofOfDeliveryAllocationRateOffsetAdjuster(0.36),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 13630.4,
    )
}
