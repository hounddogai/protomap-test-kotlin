package com.acme.transport.crewing.availability.mapping

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the rolling availability mapping calculation. */
interface RollingAvailabilityMappingFactor {
    fun describe(): String
    fun adjust(item: RollingAvailabilityMapping, value: Double): Double
}

data class FlaggedRollingAvailabilityMappingAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingAvailabilityMappingFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingAvailabilityMapping, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingRollingAvailabilityMappingAdjuster(var step: Double) : RollingAvailabilityMappingFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingAvailabilityMapping, value: Double): Double = roundTo(value, step)
}

data class RollingAvailabilityMappingRetriesOffsetAdjuster(
    var weight: Double,
) : RollingAvailabilityMappingFactor {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: RollingAvailabilityMapping, value: Double): Double {
        return value + item.retries.toDouble() * weight
    }
}

/** Runs the rolling availability mapping steps in order and keeps a trace of each. */
data class RollingAvailabilityMappingEngine(
    var steps: MutableList<RollingAvailabilityMappingFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RollingAvailabilityMapping): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingAvailabilityMappingFactor = steps[index]
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

    fun evaluateAll(items: MutableList<RollingAvailabilityMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRollingAvailabilityMappingEngine(flags: Registry): RollingAvailabilityMappingEngine {
    return RollingAvailabilityMappingEngine(
        steps = mutableListOf<RollingAvailabilityMappingFactor>(
            FlaggedRollingAvailabilityMappingAdjuster(flags, "rolling-availability-mapping-rolling", 6.1),
            RoundingRollingAvailabilityMappingAdjuster(0.1),
            RollingAvailabilityMappingRetriesOffsetAdjuster(0.74),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 11291.5,
    )
}
