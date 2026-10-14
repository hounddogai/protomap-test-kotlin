package com.acme.transport.lastmile.proofofdelivery.manifest

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the weighted proof of delivery manifest calculation. */
interface WeightedProofOfDeliveryManifestFactor {
    fun describe(): String
    fun adjust(item: WeightedProofOfDeliveryManifest, value: Double): Double
}

data class PercentWeightedProofOfDeliveryManifestAdjuster(
    var percent: Double,
) : WeightedProofOfDeliveryManifestFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedProofOfDeliveryManifest, value: Double): Double {
        return value * (1.0 + percent / 100.0)
    }
}

data class NameWeightedProofOfDeliveryManifestAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedProofOfDeliveryManifestFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: WeightedProofOfDeliveryManifest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingWeightedProofOfDeliveryManifestAdjuster(
    var step: Double,
) : WeightedProofOfDeliveryManifestFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedProofOfDeliveryManifest, value: Double): Double = roundTo(value, step)
}

data class WeightedProofOfDeliveryManifestCeilingOffsetAdjuster(
    var weight: Double,
) : WeightedProofOfDeliveryManifestFactor {
    override fun describe(): String = "plus ceiling times $weight"

    override fun adjust(item: WeightedProofOfDeliveryManifest, value: Double): Double = value + item.ceiling * weight
}

/** Runs the weighted proof of delivery manifest steps in order and keeps a trace of each. */
data class WeightedProofOfDeliveryManifestEngine(
    var steps: MutableList<WeightedProofOfDeliveryManifestFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: WeightedProofOfDeliveryManifest): Double {
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

    fun evaluateAll(items: MutableList<WeightedProofOfDeliveryManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultWeightedProofOfDeliveryManifestEngine(): WeightedProofOfDeliveryManifestEngine {
    return WeightedProofOfDeliveryManifestEngine(
        steps = mutableListOf<WeightedProofOfDeliveryManifestFactor>(
            PercentWeightedProofOfDeliveryManifestAdjuster(23.4),
            NameWeightedProofOfDeliveryManifestAdjuster(
                mutableMapOf<String, Double>("online" to 1.26, "central" to 0.73, "south" to 1.28),
            ),
            RoundingWeightedProofOfDeliveryManifestAdjuster(5.0),
            WeightedProofOfDeliveryManifestCeilingOffsetAdjuster(0.99),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 10851.0,
    )
}
