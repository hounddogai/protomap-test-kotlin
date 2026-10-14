package com.acme.transport.crewing.availability.availabilityaudit

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the availability override calculation. */
interface AvailabilityOverrideStep {
    fun describe(): String
    fun adjust(item: AvailabilityOverride, value: Double): Double
}

data class CategoryAvailabilityOverrideAdjuster(
    var factors: MutableMap<String, Double>,
) : AvailabilityOverrideStep {
    override fun describe(): String = "by category"

    override fun adjust(item: AvailabilityOverride, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumAvailabilityOverrideAdjuster(var minimum: Double) : AvailabilityOverrideStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AvailabilityOverride, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedAvailabilityOverrideAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AvailabilityOverrideStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AvailabilityOverride, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class AvailabilityOverrideWeightOffsetAdjuster(var weight: Double) : AvailabilityOverrideStep {
    override fun describe(): String = "plus weight times $weight"

    override fun adjust(item: AvailabilityOverride, value: Double): Double = value + item.weight * weight
}

data class PercentAvailabilityOverrideAdjuster(var percent: Double) : AvailabilityOverrideStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AvailabilityOverride, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredAvailabilityOverrideAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AvailabilityOverrideStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AvailabilityOverride, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the availability override steps in order and keeps a trace of each. */
data class AvailabilityOverrideEngine(
    var steps: MutableList<AvailabilityOverrideStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AvailabilityOverride): Double {
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

    fun evaluateAll(items: MutableList<AvailabilityOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAvailabilityOverrideEngine(flags: Registry): AvailabilityOverrideEngine {
    return AvailabilityOverrideEngine(
        steps = mutableListOf<AvailabilityOverrideStep>(
            CategoryAvailabilityOverrideAdjuster(
                mutableMapOf<String, Double>("north" to 0.81, "central" to 1.04),
            ),
            MinimumAvailabilityOverrideAdjuster(43.0),
            FlaggedAvailabilityOverrideAdjuster(flags, "availability-override-global", 9.8),
            AvailabilityOverrideWeightOffsetAdjuster(0.08),
            PercentAvailabilityOverrideAdjuster(13.3),
            TieredAvailabilityOverrideAdjuster(
                mutableListOf<Double>(957.0, 3917.0, 4974.0, 5300.0),
                mutableListOf<Double>(0.87, 1.05, 1.05, 0.83),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 57756.8,
    )
}
