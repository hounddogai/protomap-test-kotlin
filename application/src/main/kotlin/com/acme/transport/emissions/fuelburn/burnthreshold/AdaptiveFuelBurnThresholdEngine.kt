package com.acme.transport.emissions.fuelburn.burnthreshold

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the adaptive fuel burn threshold calculation. */
interface AdaptiveFuelBurnThresholdModifier {
    fun describe(): String
    fun adjust(item: AdaptiveFuelBurnThreshold, value: Double): Double
}

data class FlaggedAdaptiveFuelBurnThresholdAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveFuelBurnThresholdModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveFuelBurnThreshold, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapAdaptiveFuelBurnThresholdAdjuster(var cap: Double) : AdaptiveFuelBurnThresholdModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveFuelBurnThreshold, value: Double): Double = minOf(value, cap)
}

data class AdaptiveFuelBurnThresholdBatchSizeOffsetAdjuster(
    var weight: Double,
) : AdaptiveFuelBurnThresholdModifier {
    override fun describe(): String = "plus batch size times $weight"

    override fun adjust(item: AdaptiveFuelBurnThreshold, value: Double): Double {
        return value + item.batchSize.toDouble() * weight
    }
}

data class PercentAdaptiveFuelBurnThresholdAdjuster(var percent: Double) : AdaptiveFuelBurnThresholdModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveFuelBurnThreshold, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingAdaptiveFuelBurnThresholdAdjuster(var step: Double) : AdaptiveFuelBurnThresholdModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveFuelBurnThreshold, value: Double): Double = roundTo(value, step)
}

data class MinimumAdaptiveFuelBurnThresholdAdjuster(var minimum: Double) : AdaptiveFuelBurnThresholdModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveFuelBurnThreshold, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the adaptive fuel burn threshold steps in order and keeps a trace of each. */
data class AdaptiveFuelBurnThresholdEngine(
    var steps: MutableList<AdaptiveFuelBurnThresholdModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AdaptiveFuelBurnThreshold): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AdaptiveFuelBurnThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAdaptiveFuelBurnThresholdEngine(flags: Registry): AdaptiveFuelBurnThresholdEngine {
    return AdaptiveFuelBurnThresholdEngine(
        steps = mutableListOf<AdaptiveFuelBurnThresholdModifier>(
            FlaggedAdaptiveFuelBurnThresholdAdjuster(flags, "adaptive-fuel-burn-threshold-primary", 6.3),
            CapAdaptiveFuelBurnThresholdAdjuster(19113.0),
            AdaptiveFuelBurnThresholdBatchSizeOffsetAdjuster(1.48),
            PercentAdaptiveFuelBurnThresholdAdjuster(13.5),
            RoundingAdaptiveFuelBurnThresholdAdjuster(5.0),
            MinimumAdaptiveFuelBurnThresholdAdjuster(22.5),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 94190.4,
    )
}
