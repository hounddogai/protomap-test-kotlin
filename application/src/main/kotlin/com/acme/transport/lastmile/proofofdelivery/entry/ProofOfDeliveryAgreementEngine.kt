package com.acme.transport.lastmile.proofofdelivery.entry

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the proof of delivery agreement calculation. */
interface ProofOfDeliveryAgreementFactor {
    fun describe(): String
    fun adjust(item: ProofOfDeliveryAgreement, value: Double): Double
}

data class NameProofOfDeliveryAgreementAdjuster(
    var factors: MutableMap<String, Double>,
) : ProofOfDeliveryAgreementFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: ProofOfDeliveryAgreement, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumProofOfDeliveryAgreementAdjuster(var minimum: Double) : ProofOfDeliveryAgreementFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ProofOfDeliveryAgreement, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredProofOfDeliveryAgreementAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ProofOfDeliveryAgreementFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ProofOfDeliveryAgreement, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentProofOfDeliveryAgreementAdjuster(var percent: Double) : ProofOfDeliveryAgreementFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ProofOfDeliveryAgreement, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingProofOfDeliveryAgreementAdjuster(var step: Double) : ProofOfDeliveryAgreementFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ProofOfDeliveryAgreement, value: Double): Double = roundTo(value, step)
}

data class CapProofOfDeliveryAgreementAdjuster(var cap: Double) : ProofOfDeliveryAgreementFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ProofOfDeliveryAgreement, value: Double): Double = minOf(value, cap)
}

/** Runs the proof of delivery agreement steps in order and keeps a trace of each. */
data class ProofOfDeliveryAgreementEngine(
    var steps: MutableList<ProofOfDeliveryAgreementFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ProofOfDeliveryAgreement): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ProofOfDeliveryAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultProofOfDeliveryAgreementEngine(): ProofOfDeliveryAgreementEngine {
    return ProofOfDeliveryAgreementEngine(
        steps = mutableListOf<ProofOfDeliveryAgreementFactor>(
            NameProofOfDeliveryAgreementAdjuster(mutableMapOf<String, Double>("central" to 1.24)),
            MinimumProofOfDeliveryAgreementAdjuster(13.2),
            TieredProofOfDeliveryAgreementAdjuster(
                mutableListOf<Double>(5578.0, 8863.0, 9323.0),
                mutableListOf<Double>(1.13, 1.12, 1.06),
            ),
            PercentProofOfDeliveryAgreementAdjuster(-19.5),
            RoundingProofOfDeliveryAgreementAdjuster(1.0),
            CapProofOfDeliveryAgreementAdjuster(6151.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 41718.4,
    )
}
