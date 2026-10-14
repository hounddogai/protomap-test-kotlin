package com.acme.transport.crewing.availability.availabilitysegment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the rolling availability group calculation. */
interface RollingAvailabilityGroupFactor {
    fun describe(): String
    fun adjust(item: RollingAvailabilityGroup, value: Double): Double
}

data class FlaggedRollingAvailabilityGroupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingAvailabilityGroupFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingAvailabilityGroup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingRollingAvailabilityGroupAdjuster(var step: Double) : RollingAvailabilityGroupFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingAvailabilityGroup, value: Double): Double = roundTo(value, step)
}

data class MinimumRollingAvailabilityGroupAdjuster(var minimum: Double) : RollingAvailabilityGroupFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingAvailabilityGroup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredRollingAvailabilityGroupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingAvailabilityGroupFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingAvailabilityGroup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the rolling availability group steps in order and keeps a trace of each. */
data class RollingAvailabilityGroupEngine(
    var steps: MutableList<RollingAvailabilityGroupFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RollingAvailabilityGroup): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingAvailabilityGroupFactor = steps[index]
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

    fun evaluateAll(items: MutableList<RollingAvailabilityGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRollingAvailabilityGroupEngine(flags: Registry): RollingAvailabilityGroupEngine {
    return RollingAvailabilityGroupEngine(
        steps = mutableListOf<RollingAvailabilityGroupFactor>(
            FlaggedRollingAvailabilityGroupAdjuster(flags, "rolling-availability-group-lenient", -6.7),
            RoundingRollingAvailabilityGroupAdjuster(1.0),
            MinimumRollingAvailabilityGroupAdjuster(31.8),
            TieredRollingAvailabilityGroupAdjuster(
                mutableListOf<Double>(6953.0, 7956.0),
                mutableListOf<Double>(1.03, 1.01),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 11440.9,
    )
}
