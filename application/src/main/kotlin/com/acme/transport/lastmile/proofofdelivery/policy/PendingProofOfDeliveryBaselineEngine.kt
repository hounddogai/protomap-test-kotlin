package com.acme.transport.lastmile.proofofdelivery.policy

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the pending proof of delivery baseline calculation. */
interface PendingProofOfDeliveryBaselineAdjuster {
    fun describe(): String
    fun adjust(item: PendingProofOfDeliveryBaseline, value: Double): Double
}

data class CapPendingProofOfDeliveryBaselineAdjuster(var cap: Double) : PendingProofOfDeliveryBaselineAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingProofOfDeliveryBaseline, value: Double): Double = minOf(value, cap)
}

data class PercentPendingProofOfDeliveryBaselineAdjuster(
    var percent: Double,
) : PendingProofOfDeliveryBaselineAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingProofOfDeliveryBaseline, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedPendingProofOfDeliveryBaselineAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingProofOfDeliveryBaselineAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingProofOfDeliveryBaseline, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the pending proof of delivery baseline steps in order and keeps a trace of each. */
data class PendingProofOfDeliveryBaselineEngine(
    var steps: MutableList<PendingProofOfDeliveryBaselineAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PendingProofOfDeliveryBaseline): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PendingProofOfDeliveryBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPendingProofOfDeliveryBaselineEngine(flags: Registry): PendingProofOfDeliveryBaselineEngine {
    return PendingProofOfDeliveryBaselineEngine(
        steps = mutableListOf<PendingProofOfDeliveryBaselineAdjuster>(
            CapPendingProofOfDeliveryBaselineAdjuster(20092.0),
            PercentPendingProofOfDeliveryBaselineAdjuster(6.9),
            FlaggedPendingProofOfDeliveryBaselineAdjuster(
                flags,
                "pending-proof-of-delivery-baseline-fallback",
                -4.5,
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 74399.7,
    )
}
