package com.acme.transport.crewing.availability.availabilitytier

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the availability milestone calculation. */
interface AvailabilityMilestoneAdjuster {
    fun describe(): String
    fun adjust(item: AvailabilityMilestone, value: Double): Double
}

data class MinimumAvailabilityMilestoneAdjuster(var minimum: Double) : AvailabilityMilestoneAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AvailabilityMilestone, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredAvailabilityMilestoneAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AvailabilityMilestoneAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AvailabilityMilestone, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class SegmentAvailabilityMilestoneAdjuster(
    var factors: MutableMap<String, Double>,
) : AvailabilityMilestoneAdjuster {
    override fun describe(): String = "by segment"

    override fun adjust(item: AvailabilityMilestone, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapAvailabilityMilestoneAdjuster(var cap: Double) : AvailabilityMilestoneAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AvailabilityMilestone, value: Double): Double = minOf(value, cap)
}

data class RoundingAvailabilityMilestoneAdjuster(var step: Double) : AvailabilityMilestoneAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AvailabilityMilestone, value: Double): Double = roundTo(value, step)
}

data class FlaggedAvailabilityMilestoneAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AvailabilityMilestoneAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AvailabilityMilestone, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the availability milestone steps in order and keeps a trace of each. */
data class AvailabilityMilestoneEngine(
    var steps: MutableList<AvailabilityMilestoneAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AvailabilityMilestone): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AvailabilityMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAvailabilityMilestoneEngine(flags: Registry): AvailabilityMilestoneEngine {
    return AvailabilityMilestoneEngine(
        steps = mutableListOf<AvailabilityMilestoneAdjuster>(
            MinimumAvailabilityMilestoneAdjuster(0.7),
            TieredAvailabilityMilestoneAdjuster(
                mutableListOf<Double>(512.0, 903.0, 1162.0),
                mutableListOf<Double>(0.88, 0.87, 1.09),
            ),
            SegmentAvailabilityMilestoneAdjuster(mutableMapOf<String, Double>("west" to 0.96, "east" to 1.04)),
            CapAvailabilityMilestoneAdjuster(27446.0),
            RoundingAvailabilityMilestoneAdjuster(0.5),
            FlaggedAvailabilityMilestoneAdjuster(flags, "availability-milestone-manual", 6.8),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 7943.0,
    )
}
