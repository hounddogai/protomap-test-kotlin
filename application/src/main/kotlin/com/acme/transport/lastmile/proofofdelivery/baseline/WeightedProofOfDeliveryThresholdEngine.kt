package com.acme.transport.lastmile.proofofdelivery.baseline

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the weighted proof of delivery threshold calculation. */
interface WeightedProofOfDeliveryThresholdFactor {
    fun describe(): String
    fun adjust(item: WeightedProofOfDeliveryThreshold, value: Double): Double
}

data class PercentWeightedProofOfDeliveryThresholdAdjuster(
    var percent: Double,
) : WeightedProofOfDeliveryThresholdFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedProofOfDeliveryThreshold, value: Double): Double {
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingWeightedProofOfDeliveryThresholdAdjuster(
    var step: Double,
) : WeightedProofOfDeliveryThresholdFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedProofOfDeliveryThreshold, value: Double): Double = roundTo(value, step)
}

data class WeightedProofOfDeliveryThresholdSequenceOffsetAdjuster(
    var weight: Double,
) : WeightedProofOfDeliveryThresholdFactor {
    override fun describe(): String = "plus sequence times $weight"

    override fun adjust(item: WeightedProofOfDeliveryThreshold, value: Double): Double {
        return value + item.sequence.toDouble() * weight
    }
}

/** Runs the weighted proof of delivery threshold steps in order and keeps a trace of each. */
data class WeightedProofOfDeliveryThresholdEngine(
    var steps: MutableList<WeightedProofOfDeliveryThresholdFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: WeightedProofOfDeliveryThreshold): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<WeightedProofOfDeliveryThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultWeightedProofOfDeliveryThresholdEngine(): WeightedProofOfDeliveryThresholdEngine {
    return WeightedProofOfDeliveryThresholdEngine(
        steps = mutableListOf<WeightedProofOfDeliveryThresholdFactor>(
            PercentWeightedProofOfDeliveryThresholdAdjuster(18.5),
            RoundingWeightedProofOfDeliveryThresholdAdjuster(0.25),
            WeightedProofOfDeliveryThresholdSequenceOffsetAdjuster(1.73),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 74742.9,
    )
}
