package com.acme.transport.lastmile.proofofdelivery.group

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the proof of delivery group calculation. */
interface ProofOfDeliveryGroupFactor {
    fun describe(): String
    fun adjust(item: ProofOfDeliveryGroup, value: Double): Double
}

data class RoundingProofOfDeliveryGroupAdjuster(var step: Double) : ProofOfDeliveryGroupFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ProofOfDeliveryGroup, value: Double): Double = roundTo(value, step)
}

data class PercentProofOfDeliveryGroupAdjuster(var percent: Double) : ProofOfDeliveryGroupFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ProofOfDeliveryGroup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredProofOfDeliveryGroupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ProofOfDeliveryGroupFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ProofOfDeliveryGroup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapProofOfDeliveryGroupAdjuster(var cap: Double) : ProofOfDeliveryGroupFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ProofOfDeliveryGroup, value: Double): Double = minOf(value, cap)
}

data class MinimumProofOfDeliveryGroupAdjuster(var minimum: Double) : ProofOfDeliveryGroupFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ProofOfDeliveryGroup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the proof of delivery group steps in order and keeps a trace of each. */
data class ProofOfDeliveryGroupEngine(
    var steps: MutableList<ProofOfDeliveryGroupFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ProofOfDeliveryGroup): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ProofOfDeliveryGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultProofOfDeliveryGroupEngine(): ProofOfDeliveryGroupEngine {
    return ProofOfDeliveryGroupEngine(
        steps = mutableListOf<ProofOfDeliveryGroupFactor>(
            RoundingProofOfDeliveryGroupAdjuster(0.1),
            PercentProofOfDeliveryGroupAdjuster(16.7),
            TieredProofOfDeliveryGroupAdjuster(
                mutableListOf<Double>(5664.0, 6767.0, 7951.0),
                mutableListOf<Double>(0.97, 0.81, 0.97),
            ),
            CapProofOfDeliveryGroupAdjuster(638.0),
            MinimumProofOfDeliveryGroupAdjuster(22.4),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 63354.5,
    )
}
