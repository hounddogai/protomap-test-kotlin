package com.acme.transport.lastmile.proofofdelivery.review

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the capped proof of delivery profile calculation. */
interface CappedProofOfDeliveryProfileFactor {
    fun describe(): String
    fun adjust(item: CappedProofOfDeliveryProfile, value: Double): Double
}

data class PercentCappedProofOfDeliveryProfileAdjuster(
    var percent: Double,
) : CappedProofOfDeliveryProfileFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedProofOfDeliveryProfile, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CappedProofOfDeliveryProfileCapacityOffsetAdjuster(
    var weight: Double,
) : CappedProofOfDeliveryProfileFactor {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: CappedProofOfDeliveryProfile, value: Double): Double {
        return value + item.capacity.toDouble() * weight
    }
}

data class NameCappedProofOfDeliveryProfileAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedProofOfDeliveryProfileFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: CappedProofOfDeliveryProfile, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumCappedProofOfDeliveryProfileAdjuster(
    var minimum: Double,
) : CappedProofOfDeliveryProfileFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedProofOfDeliveryProfile, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the capped proof of delivery profile steps in order and keeps a trace of each. */
data class CappedProofOfDeliveryProfileEngine(
    var steps: MutableList<CappedProofOfDeliveryProfileFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CappedProofOfDeliveryProfile): Double {
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

    fun evaluateAll(items: MutableList<CappedProofOfDeliveryProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCappedProofOfDeliveryProfileEngine(): CappedProofOfDeliveryProfileEngine {
    return CappedProofOfDeliveryProfileEngine(
        steps = mutableListOf<CappedProofOfDeliveryProfileFactor>(
            PercentCappedProofOfDeliveryProfileAdjuster(23.3),
            CappedProofOfDeliveryProfileCapacityOffsetAdjuster(0.16),
            NameCappedProofOfDeliveryProfileAdjuster(mutableMapOf<String, Double>("west" to 0.8)),
            MinimumCappedProofOfDeliveryProfileAdjuster(16.3),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 1305.0,
    )
}
