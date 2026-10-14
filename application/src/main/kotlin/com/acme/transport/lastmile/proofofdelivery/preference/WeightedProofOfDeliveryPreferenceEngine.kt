package com.acme.transport.lastmile.proofofdelivery.preference

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the weighted proof of delivery preference calculation. */
interface WeightedProofOfDeliveryPreferenceStep {
    fun describe(): String
    fun adjust(item: WeightedProofOfDeliveryPreference, value: Double): Double
}

data class TieredWeightedProofOfDeliveryPreferenceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedProofOfDeliveryPreferenceStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedProofOfDeliveryPreference, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentWeightedProofOfDeliveryPreferenceAdjuster(
    var percent: Double,
) : WeightedProofOfDeliveryPreferenceStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedProofOfDeliveryPreference, value: Double): Double {
        return value * (1.0 + percent / 100.0)
    }
}

data class WeightedProofOfDeliveryPreferenceMarginOffsetAdjuster(
    var weight: Double,
) : WeightedProofOfDeliveryPreferenceStep {
    override fun describe(): String = "plus margin times $weight"

    override fun adjust(item: WeightedProofOfDeliveryPreference, value: Double): Double = value + item.margin * weight
}

/** Runs the weighted proof of delivery preference steps in order and keeps a trace of each. */
data class WeightedProofOfDeliveryPreferenceEngine(
    var steps: MutableList<WeightedProofOfDeliveryPreferenceStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: WeightedProofOfDeliveryPreference): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<WeightedProofOfDeliveryPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultWeightedProofOfDeliveryPreferenceEngine(): WeightedProofOfDeliveryPreferenceEngine {
    return WeightedProofOfDeliveryPreferenceEngine(
        steps = mutableListOf<WeightedProofOfDeliveryPreferenceStep>(
            TieredWeightedProofOfDeliveryPreferenceAdjuster(
                mutableListOf<Double>(641.0, 4719.0),
                mutableListOf<Double>(1.17, 1.12),
            ),
            PercentWeightedProofOfDeliveryPreferenceAdjuster(15.2),
            WeightedProofOfDeliveryPreferenceMarginOffsetAdjuster(0.32),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 37488.4,
    )
}
