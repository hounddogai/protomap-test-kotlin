package com.acme.transport.lastmile.proofofdelivery.deliveryrollup

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the proof of delivery checkpoint calculation. */
interface ProofOfDeliveryCheckpointStep {
    fun describe(): String
    fun adjust(item: ProofOfDeliveryCheckpoint, value: Double): Double
}

data class MinimumProofOfDeliveryCheckpointAdjuster(var minimum: Double) : ProofOfDeliveryCheckpointStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ProofOfDeliveryCheckpoint, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentProofOfDeliveryCheckpointAdjuster(var percent: Double) : ProofOfDeliveryCheckpointStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ProofOfDeliveryCheckpoint, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapProofOfDeliveryCheckpointAdjuster(var cap: Double) : ProofOfDeliveryCheckpointStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ProofOfDeliveryCheckpoint, value: Double): Double = minOf(value, cap)
}

data class FlaggedProofOfDeliveryCheckpointAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ProofOfDeliveryCheckpointStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ProofOfDeliveryCheckpoint, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredProofOfDeliveryCheckpointAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ProofOfDeliveryCheckpointStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ProofOfDeliveryCheckpoint, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the proof of delivery checkpoint steps in order and keeps a trace of each. */
data class ProofOfDeliveryCheckpointEngine(
    var steps: MutableList<ProofOfDeliveryCheckpointStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ProofOfDeliveryCheckpoint): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ProofOfDeliveryCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultProofOfDeliveryCheckpointEngine(flags: Registry): ProofOfDeliveryCheckpointEngine {
    return ProofOfDeliveryCheckpointEngine(
        steps = mutableListOf<ProofOfDeliveryCheckpointStep>(
            MinimumProofOfDeliveryCheckpointAdjuster(40.6),
            PercentProofOfDeliveryCheckpointAdjuster(18.7),
            CapProofOfDeliveryCheckpointAdjuster(46928.0),
            FlaggedProofOfDeliveryCheckpointAdjuster(flags, "proof-of-delivery-checkpoint-default", 0.3),
            TieredProofOfDeliveryCheckpointAdjuster(
                mutableListOf<Double>(3825.0, 6185.0, 6981.0, 9884.0),
                mutableListOf<Double>(1.0, 0.82, 0.88, 1.2),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 33534.4,
    )
}
