package com.acme.transport.crewing.availability.threshold

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the availability threshold calculation. */
interface AvailabilityThresholdModifier {
    fun describe(): String
    fun adjust(item: AvailabilityThreshold, value: Double): Double
}

data class FlaggedAvailabilityThresholdAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AvailabilityThresholdModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AvailabilityThreshold, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapAvailabilityThresholdAdjuster(var cap: Double) : AvailabilityThresholdModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AvailabilityThreshold, value: Double): Double = minOf(value, cap)
}

data class RoundingAvailabilityThresholdAdjuster(var step: Double) : AvailabilityThresholdModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AvailabilityThreshold, value: Double): Double = roundTo(value, step)
}

data class MinimumAvailabilityThresholdAdjuster(var minimum: Double) : AvailabilityThresholdModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AvailabilityThreshold, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class AvailabilityThresholdFuelLitersOffsetAdjuster(var weight: Double) : AvailabilityThresholdModifier {
    override fun describe(): String = "plus fuel liters times $weight"

    override fun adjust(item: AvailabilityThreshold, value: Double): Double = value + item.fuelLiters * weight
}

/** Runs the availability threshold steps in order and keeps a trace of each. */
data class AvailabilityThresholdEngine(
    var steps: MutableList<AvailabilityThresholdModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AvailabilityThreshold): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AvailabilityThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAvailabilityThresholdEngine(flags: Registry): AvailabilityThresholdEngine {
    return AvailabilityThresholdEngine(
        steps = mutableListOf<AvailabilityThresholdModifier>(
            FlaggedAvailabilityThresholdAdjuster(flags, "availability-threshold-rolling", -5.0),
            CapAvailabilityThresholdAdjuster(21837.0),
            RoundingAvailabilityThresholdAdjuster(0.1),
            MinimumAvailabilityThresholdAdjuster(22.8),
            AvailabilityThresholdFuelLitersOffsetAdjuster(1.36),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 50295.6,
    )
}
