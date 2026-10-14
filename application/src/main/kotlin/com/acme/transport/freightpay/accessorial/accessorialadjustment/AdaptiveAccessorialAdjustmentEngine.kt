package com.acme.transport.freightpay.accessorial.accessorialadjustment

import com.acme.transport.kit.numbers.roundTo

/** One step of the adaptive accessorial adjustment calculation. */
interface AdaptiveAccessorialAdjustmentStep {
    fun describe(): String
    fun adjust(item: AdaptiveAccessorialAdjustment, value: Double): Double
}

data class MinimumAdaptiveAccessorialAdjustmentAdjuster(
    var minimum: Double,
) : AdaptiveAccessorialAdjustmentStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveAccessorialAdjustment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentAdaptiveAccessorialAdjustmentAdjuster(
    var percent: Double,
) : AdaptiveAccessorialAdjustmentStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveAccessorialAdjustment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredAdaptiveAccessorialAdjustmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveAccessorialAdjustmentStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveAccessorialAdjustment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingAdaptiveAccessorialAdjustmentAdjuster(var step: Double) : AdaptiveAccessorialAdjustmentStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveAccessorialAdjustment, value: Double): Double = roundTo(value, step)
}

/** Runs the adaptive accessorial adjustment steps in order and keeps a trace of each. */
data class AdaptiveAccessorialAdjustmentEngine(
    var steps: MutableList<AdaptiveAccessorialAdjustmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AdaptiveAccessorialAdjustment): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveAccessorialAdjustmentStep = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveAccessorialAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAdaptiveAccessorialAdjustmentEngine(): AdaptiveAccessorialAdjustmentEngine {
    return AdaptiveAccessorialAdjustmentEngine(
        steps = mutableListOf<AdaptiveAccessorialAdjustmentStep>(
            MinimumAdaptiveAccessorialAdjustmentAdjuster(15.2),
            PercentAdaptiveAccessorialAdjustmentAdjuster(-13.3),
            TieredAdaptiveAccessorialAdjustmentAdjuster(
                mutableListOf<Double>(547.0, 2432.0, 6169.0, 8657.0),
                mutableListOf<Double>(1.15, 1.03, 1.14, 1.01),
            ),
            RoundingAdaptiveAccessorialAdjustmentAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 83710.7,
    )
}
