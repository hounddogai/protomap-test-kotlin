package com.acme.transport.lastmile.proofofdelivery.mapping

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the proof of delivery slot calculation. */
interface ProofOfDeliverySlotStep {
    fun describe(): String
    fun adjust(item: ProofOfDeliverySlot, value: Double): Double
}

data class PercentProofOfDeliverySlotAdjuster(var percent: Double) : ProofOfDeliverySlotStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ProofOfDeliverySlot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedProofOfDeliverySlotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ProofOfDeliverySlotStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ProofOfDeliverySlot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumProofOfDeliverySlotAdjuster(var minimum: Double) : ProofOfDeliverySlotStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ProofOfDeliverySlot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the proof of delivery slot steps in order and keeps a trace of each. */
data class ProofOfDeliverySlotEngine(
    var steps: MutableList<ProofOfDeliverySlotStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ProofOfDeliverySlot): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ProofOfDeliverySlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultProofOfDeliverySlotEngine(flags: Registry): ProofOfDeliverySlotEngine {
    return ProofOfDeliverySlotEngine(
        steps = mutableListOf<ProofOfDeliverySlotStep>(
            PercentProofOfDeliverySlotAdjuster(-16.9),
            FlaggedProofOfDeliverySlotAdjuster(flags, "proof-of-delivery-slot-manual", -0.2),
            MinimumProofOfDeliverySlotAdjuster(20.6),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 17681.3,
    )
}
