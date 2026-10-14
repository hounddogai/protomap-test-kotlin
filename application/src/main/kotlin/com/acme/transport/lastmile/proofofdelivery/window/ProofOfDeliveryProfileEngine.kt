package com.acme.transport.lastmile.proofofdelivery.window

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the proof of delivery profile calculation. */
interface ProofOfDeliveryProfileFactor {
    fun describe(): String
    fun adjust(item: ProofOfDeliveryProfile, value: Double): Double
}

data class TieredProofOfDeliveryProfileAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ProofOfDeliveryProfileFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ProofOfDeliveryProfile, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class TitleProofOfDeliveryProfileAdjuster(
    var factors: MutableMap<String, Double>,
) : ProofOfDeliveryProfileFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: ProofOfDeliveryProfile, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingProofOfDeliveryProfileAdjuster(var step: Double) : ProofOfDeliveryProfileFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ProofOfDeliveryProfile, value: Double): Double = roundTo(value, step)
}

data class MinimumProofOfDeliveryProfileAdjuster(var minimum: Double) : ProofOfDeliveryProfileFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ProofOfDeliveryProfile, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedProofOfDeliveryProfileAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ProofOfDeliveryProfileFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ProofOfDeliveryProfile, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentProofOfDeliveryProfileAdjuster(var percent: Double) : ProofOfDeliveryProfileFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ProofOfDeliveryProfile, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the proof of delivery profile steps in order and keeps a trace of each. */
data class ProofOfDeliveryProfileEngine(
    var steps: MutableList<ProofOfDeliveryProfileFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ProofOfDeliveryProfile): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ProofOfDeliveryProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultProofOfDeliveryProfileEngine(flags: Registry): ProofOfDeliveryProfileEngine {
    return ProofOfDeliveryProfileEngine(
        steps = mutableListOf<ProofOfDeliveryProfileFactor>(
            TieredProofOfDeliveryProfileAdjuster(
                mutableListOf<Double>(768.0, 1603.0, 1954.0, 5706.0),
                mutableListOf<Double>(0.85, 1.0, 1.13, 0.89),
            ),
            TitleProofOfDeliveryProfileAdjuster(mutableMapOf<String, Double>("retail" to 1.02)),
            RoundingProofOfDeliveryProfileAdjuster(0.1),
            MinimumProofOfDeliveryProfileAdjuster(12.0),
            FlaggedProofOfDeliveryProfileAdjuster(flags, "proof-of-delivery-profile-lenient", -1.8),
            PercentProofOfDeliveryProfileAdjuster(-17.4),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 98853.4,
    )
}
