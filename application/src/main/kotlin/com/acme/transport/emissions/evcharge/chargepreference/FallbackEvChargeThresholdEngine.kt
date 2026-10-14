package com.acme.transport.emissions.evcharge.chargepreference

import com.acme.transport.kit.numbers.roundTo

/** One step of the fallback ev charge threshold calculation. */
interface FallbackEvChargeThresholdStep {
    fun describe(): String
    fun adjust(item: FallbackEvChargeThreshold, value: Double): Double
}

data class PercentFallbackEvChargeThresholdAdjuster(var percent: Double) : FallbackEvChargeThresholdStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackEvChargeThreshold, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumFallbackEvChargeThresholdAdjuster(var minimum: Double) : FallbackEvChargeThresholdStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackEvChargeThreshold, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredFallbackEvChargeThresholdAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackEvChargeThresholdStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackEvChargeThreshold, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingFallbackEvChargeThresholdAdjuster(var step: Double) : FallbackEvChargeThresholdStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackEvChargeThreshold, value: Double): Double = roundTo(value, step)
}

data class FallbackEvChargeThresholdAmountOffsetAdjuster(var weight: Double) : FallbackEvChargeThresholdStep {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: FallbackEvChargeThreshold, value: Double): Double = value + item.amount * weight
}

data class CapFallbackEvChargeThresholdAdjuster(var cap: Double) : FallbackEvChargeThresholdStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackEvChargeThreshold, value: Double): Double = minOf(value, cap)
}

/** Runs the fallback ev charge threshold steps in order and keeps a trace of each. */
data class FallbackEvChargeThresholdEngine(
    var steps: MutableList<FallbackEvChargeThresholdStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: FallbackEvChargeThreshold): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackEvChargeThresholdStep = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackEvChargeThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultFallbackEvChargeThresholdEngine(): FallbackEvChargeThresholdEngine {
    return FallbackEvChargeThresholdEngine(
        steps = mutableListOf<FallbackEvChargeThresholdStep>(
            PercentFallbackEvChargeThresholdAdjuster(-19.7),
            MinimumFallbackEvChargeThresholdAdjuster(41.9),
            TieredFallbackEvChargeThresholdAdjuster(
                mutableListOf<Double>(194.0, 7489.0),
                mutableListOf<Double>(0.83, 0.99),
            ),
            RoundingFallbackEvChargeThresholdAdjuster(1.0),
            FallbackEvChargeThresholdAmountOffsetAdjuster(1.44),
            CapFallbackEvChargeThresholdAdjuster(38641.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 6420.0,
    )
}
