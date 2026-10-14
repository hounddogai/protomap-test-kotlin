package com.acme.transport.lastmile.proofofdelivery.estimate

import com.acme.transport.kit.numbers.roundTo

/** One step of the adaptive proof of delivery threshold calculation. */
interface AdaptiveProofOfDeliveryThresholdAdjuster {
    fun describe(): String
    fun adjust(item: AdaptiveProofOfDeliveryThreshold, value: Double): Double
}

data class MinimumAdaptiveProofOfDeliveryThresholdAdjuster(
    var minimum: Double,
) : AdaptiveProofOfDeliveryThresholdAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveProofOfDeliveryThreshold, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentAdaptiveProofOfDeliveryThresholdAdjuster(
    var percent: Double,
) : AdaptiveProofOfDeliveryThresholdAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveProofOfDeliveryThreshold, value: Double): Double {
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredAdaptiveProofOfDeliveryThresholdAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveProofOfDeliveryThresholdAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveProofOfDeliveryThreshold, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class AdaptiveProofOfDeliveryThresholdWeightOffsetAdjuster(
    var weight: Double,
) : AdaptiveProofOfDeliveryThresholdAdjuster {
    override fun describe(): String = "plus weight times $weight"

    override fun adjust(item: AdaptiveProofOfDeliveryThreshold, value: Double): Double = value + item.weight * weight
}

data class CapAdaptiveProofOfDeliveryThresholdAdjuster(
    var cap: Double,
) : AdaptiveProofOfDeliveryThresholdAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveProofOfDeliveryThreshold, value: Double): Double = minOf(value, cap)
}

data class RoundingAdaptiveProofOfDeliveryThresholdAdjuster(
    var step: Double,
) : AdaptiveProofOfDeliveryThresholdAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveProofOfDeliveryThreshold, value: Double): Double = roundTo(value, step)
}

/** Runs the adaptive proof of delivery threshold steps in order and keeps a trace of each. */
data class AdaptiveProofOfDeliveryThresholdEngine(
    var steps: MutableList<AdaptiveProofOfDeliveryThresholdAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AdaptiveProofOfDeliveryThreshold): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveProofOfDeliveryThresholdAdjuster = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AdaptiveProofOfDeliveryThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAdaptiveProofOfDeliveryThresholdEngine(): AdaptiveProofOfDeliveryThresholdEngine {
    return AdaptiveProofOfDeliveryThresholdEngine(
        steps = mutableListOf<AdaptiveProofOfDeliveryThresholdAdjuster>(
            MinimumAdaptiveProofOfDeliveryThresholdAdjuster(15.4),
            PercentAdaptiveProofOfDeliveryThresholdAdjuster(-22.9),
            TieredAdaptiveProofOfDeliveryThresholdAdjuster(
                mutableListOf<Double>(246.0, 6404.0, 7848.0, 7947.0),
                mutableListOf<Double>(1.08, 0.83, 0.81, 1.0),
            ),
            AdaptiveProofOfDeliveryThresholdWeightOffsetAdjuster(1.4),
            CapAdaptiveProofOfDeliveryThresholdAdjuster(7084.0),
            RoundingAdaptiveProofOfDeliveryThresholdAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 79482.5,
    )
}
