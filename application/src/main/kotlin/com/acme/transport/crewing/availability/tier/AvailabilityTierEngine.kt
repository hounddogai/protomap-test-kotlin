package com.acme.transport.crewing.availability.tier

import com.acme.transport.kit.flags.Registry

/** One step of the availability tier calculation. */
interface AvailabilityTierAdjuster {
    fun describe(): String
    fun adjust(item: AvailabilityTier, value: Double): Double
}

data class FlaggedAvailabilityTierAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AvailabilityTierAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AvailabilityTier, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class AvailabilityTierFloorValueOffsetAdjuster(var weight: Double) : AvailabilityTierAdjuster {
    override fun describe(): String = "plus floor value times $weight"

    override fun adjust(item: AvailabilityTier, value: Double): Double = value + item.floorValue * weight
}

data class MinimumAvailabilityTierAdjuster(var minimum: Double) : AvailabilityTierAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AvailabilityTier, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the availability tier steps in order and keeps a trace of each. */
data class AvailabilityTierEngine(
    var steps: MutableList<AvailabilityTierAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AvailabilityTier): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AvailabilityTierAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<AvailabilityTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAvailabilityTierEngine(flags: Registry): AvailabilityTierEngine {
    return AvailabilityTierEngine(
        steps = mutableListOf<AvailabilityTierAdjuster>(
            FlaggedAvailabilityTierAdjuster(flags, "availability-tier-primary", -8.9),
            AvailabilityTierFloorValueOffsetAdjuster(0.41),
            MinimumAvailabilityTierAdjuster(20.5),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 97596.9,
    )
}
