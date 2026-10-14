package com.acme.transport.lastmile.proofofdelivery.score

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the proof of delivery adjustment calculation. */
interface ProofOfDeliveryAdjustmentModifier {
    fun describe(): String
    fun adjust(item: ProofOfDeliveryAdjustment, value: Double): Double
}

data class TieredProofOfDeliveryAdjustmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ProofOfDeliveryAdjustmentModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ProofOfDeliveryAdjustment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class TitleProofOfDeliveryAdjustmentAdjuster(
    var factors: MutableMap<String, Double>,
) : ProofOfDeliveryAdjustmentModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: ProofOfDeliveryAdjustment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumProofOfDeliveryAdjustmentAdjuster(var minimum: Double) : ProofOfDeliveryAdjustmentModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ProofOfDeliveryAdjustment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingProofOfDeliveryAdjustmentAdjuster(var step: Double) : ProofOfDeliveryAdjustmentModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ProofOfDeliveryAdjustment, value: Double): Double = roundTo(value, step)
}

data class PercentProofOfDeliveryAdjustmentAdjuster(var percent: Double) : ProofOfDeliveryAdjustmentModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ProofOfDeliveryAdjustment, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the proof of delivery adjustment steps in order and keeps a trace of each. */
data class ProofOfDeliveryAdjustmentEngine(
    var steps: MutableList<ProofOfDeliveryAdjustmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ProofOfDeliveryAdjustment): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ProofOfDeliveryAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultProofOfDeliveryAdjustmentEngine(): ProofOfDeliveryAdjustmentEngine {
    return ProofOfDeliveryAdjustmentEngine(
        steps = mutableListOf<ProofOfDeliveryAdjustmentModifier>(
            TieredProofOfDeliveryAdjustmentAdjuster(
                mutableListOf<Double>(765.0, 6747.0, 7533.0),
                mutableListOf<Double>(0.82, 0.96, 1.06),
            ),
            TitleProofOfDeliveryAdjustmentAdjuster(mutableMapOf<String, Double>("east" to 1.27)),
            MinimumProofOfDeliveryAdjustmentAdjuster(11.8),
            RoundingProofOfDeliveryAdjustmentAdjuster(1.0),
            PercentProofOfDeliveryAdjustmentAdjuster(22.6),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 64172.0,
    )
}
