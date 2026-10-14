package com.acme.transport.lastmile.proofofdelivery.notice

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the adaptive proof of delivery record calculation. */
interface AdaptiveProofOfDeliveryRecordAdjuster {
    fun describe(): String
    fun adjust(item: AdaptiveProofOfDeliveryRecord, value: Double): Double
}

data class MinimumAdaptiveProofOfDeliveryRecordAdjuster(
    var minimum: Double,
) : AdaptiveProofOfDeliveryRecordAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveProofOfDeliveryRecord, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapAdaptiveProofOfDeliveryRecordAdjuster(var cap: Double) : AdaptiveProofOfDeliveryRecordAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveProofOfDeliveryRecord, value: Double): Double = minOf(value, cap)
}

data class NameAdaptiveProofOfDeliveryRecordAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveProofOfDeliveryRecordAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: AdaptiveProofOfDeliveryRecord, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the adaptive proof of delivery record steps in order and keeps a trace of each. */
data class AdaptiveProofOfDeliveryRecordEngine(
    var steps: MutableList<AdaptiveProofOfDeliveryRecordAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AdaptiveProofOfDeliveryRecord): Double {
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

    fun evaluateAll(items: MutableList<AdaptiveProofOfDeliveryRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAdaptiveProofOfDeliveryRecordEngine(): AdaptiveProofOfDeliveryRecordEngine {
    return AdaptiveProofOfDeliveryRecordEngine(
        steps = mutableListOf<AdaptiveProofOfDeliveryRecordAdjuster>(
            MinimumAdaptiveProofOfDeliveryRecordAdjuster(32.0),
            CapAdaptiveProofOfDeliveryRecordAdjuster(7751.0),
            NameAdaptiveProofOfDeliveryRecordAdjuster(
                mutableMapOf<String, Double>("retail" to 1.03, "online" to 1.14),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 31053.2,
    )
}
