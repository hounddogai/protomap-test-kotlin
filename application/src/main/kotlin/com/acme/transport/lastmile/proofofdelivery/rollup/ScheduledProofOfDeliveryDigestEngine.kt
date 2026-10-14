package com.acme.transport.lastmile.proofofdelivery.rollup

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the scheduled proof of delivery digest calculation. */
interface ScheduledProofOfDeliveryDigestFactor {
    fun describe(): String
    fun adjust(item: ScheduledProofOfDeliveryDigest, value: Double): Double
}

data class TieredScheduledProofOfDeliveryDigestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledProofOfDeliveryDigestFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledProofOfDeliveryDigest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapScheduledProofOfDeliveryDigestAdjuster(var cap: Double) : ScheduledProofOfDeliveryDigestFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledProofOfDeliveryDigest, value: Double): Double = minOf(value, cap)
}

data class MinimumScheduledProofOfDeliveryDigestAdjuster(
    var minimum: Double,
) : ScheduledProofOfDeliveryDigestFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledProofOfDeliveryDigest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the scheduled proof of delivery digest steps in order and keeps a trace of each. */
data class ScheduledProofOfDeliveryDigestEngine(
    var steps: MutableList<ScheduledProofOfDeliveryDigestFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ScheduledProofOfDeliveryDigest): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ScheduledProofOfDeliveryDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultScheduledProofOfDeliveryDigestEngine(): ScheduledProofOfDeliveryDigestEngine {
    return ScheduledProofOfDeliveryDigestEngine(
        steps = mutableListOf<ScheduledProofOfDeliveryDigestFactor>(
            TieredScheduledProofOfDeliveryDigestAdjuster(
                mutableListOf<Double>(1450.0, 5195.0, 5467.0, 7753.0),
                mutableListOf<Double>(1.02, 1.12, 0.83, 1.03),
            ),
            CapScheduledProofOfDeliveryDigestAdjuster(17764.0),
            MinimumScheduledProofOfDeliveryDigestAdjuster(41.3),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 97437.3,
    )
}
