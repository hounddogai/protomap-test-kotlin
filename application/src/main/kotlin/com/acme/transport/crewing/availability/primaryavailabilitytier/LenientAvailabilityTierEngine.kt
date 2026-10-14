package com.acme.transport.crewing.availability.primaryavailabilitytier

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the lenient availability tier calculation. */
interface LenientAvailabilityTierModifier {
    fun describe(): String
    fun adjust(item: LenientAvailabilityTier, value: Double): Double
}

data class PercentLenientAvailabilityTierAdjuster(var percent: Double) : LenientAvailabilityTierModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientAvailabilityTier, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingLenientAvailabilityTierAdjuster(var step: Double) : LenientAvailabilityTierModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientAvailabilityTier, value: Double): Double = roundTo(value, step)
}

data class NameLenientAvailabilityTierAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientAvailabilityTierModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: LenientAvailabilityTier, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumLenientAvailabilityTierAdjuster(var minimum: Double) : LenientAvailabilityTierModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientAvailabilityTier, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the lenient availability tier steps in order and keeps a trace of each. */
data class LenientAvailabilityTierEngine(
    var steps: MutableList<LenientAvailabilityTierModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LenientAvailabilityTier): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LenientAvailabilityTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLenientAvailabilityTierEngine(): LenientAvailabilityTierEngine {
    return LenientAvailabilityTierEngine(
        steps = mutableListOf<LenientAvailabilityTierModifier>(
            PercentLenientAvailabilityTierAdjuster(23.0),
            RoundingLenientAvailabilityTierAdjuster(0.5),
            NameLenientAvailabilityTierAdjuster(
                mutableMapOf<String, Double>("central" to 0.87, "online" to 0.91),
            ),
            MinimumLenientAvailabilityTierAdjuster(36.9),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 84155.7,
    )
}
