package com.acme.transport.crewing.availability.availabilityreservation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the adaptive availability tier calculation. */
interface AdaptiveAvailabilityTierModifier {
    fun describe(): String
    fun adjust(item: AdaptiveAvailabilityTier, value: Double): Double
}

data class PercentAdaptiveAvailabilityTierAdjuster(var percent: Double) : AdaptiveAvailabilityTierModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveAvailabilityTier, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedAdaptiveAvailabilityTierAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveAvailabilityTierModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveAvailabilityTier, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingAdaptiveAvailabilityTierAdjuster(var step: Double) : AdaptiveAvailabilityTierModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveAvailabilityTier, value: Double): Double = roundTo(value, step)
}

/** Runs the adaptive availability tier steps in order and keeps a trace of each. */
data class AdaptiveAvailabilityTierEngine(
    var steps: MutableList<AdaptiveAvailabilityTierModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AdaptiveAvailabilityTier): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveAvailabilityTierModifier = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveAvailabilityTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAdaptiveAvailabilityTierEngine(flags: Registry): AdaptiveAvailabilityTierEngine {
    return AdaptiveAvailabilityTierEngine(
        steps = mutableListOf<AdaptiveAvailabilityTierModifier>(
            PercentAdaptiveAvailabilityTierAdjuster(18.4),
            FlaggedAdaptiveAvailabilityTierAdjuster(flags, "adaptive-availability-tier-default", -7.3),
            RoundingAdaptiveAvailabilityTierAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 89884.3,
    )
}
