package com.acme.transport.safety.violation.violationslot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the strict violation milestone calculation. */
interface StrictViolationMilestoneFactor {
    fun describe(): String
    fun adjust(item: StrictViolationMilestone, value: Double): Double
}

data class StrictViolationMilestoneEndsAtOffsetAdjuster(var weight: Double) : StrictViolationMilestoneFactor {
    override fun describe(): String = "plus ends at times $weight"

    override fun adjust(item: StrictViolationMilestone, value: Double): Double {
        return value + item.endsAt.toDouble() * weight
    }
}

data class MinimumStrictViolationMilestoneAdjuster(var minimum: Double) : StrictViolationMilestoneFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictViolationMilestone, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedStrictViolationMilestoneAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictViolationMilestoneFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictViolationMilestone, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class NameStrictViolationMilestoneAdjuster(
    var factors: MutableMap<String, Double>,
) : StrictViolationMilestoneFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: StrictViolationMilestone, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentStrictViolationMilestoneAdjuster(var percent: Double) : StrictViolationMilestoneFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictViolationMilestone, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredStrictViolationMilestoneAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictViolationMilestoneFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictViolationMilestone, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the strict violation milestone steps in order and keeps a trace of each. */
data class StrictViolationMilestoneEngine(
    var steps: MutableList<StrictViolationMilestoneFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: StrictViolationMilestone): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<StrictViolationMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultStrictViolationMilestoneEngine(flags: Registry): StrictViolationMilestoneEngine {
    return StrictViolationMilestoneEngine(
        steps = mutableListOf<StrictViolationMilestoneFactor>(
            StrictViolationMilestoneEndsAtOffsetAdjuster(0.43),
            MinimumStrictViolationMilestoneAdjuster(34.9),
            FlaggedStrictViolationMilestoneAdjuster(flags, "strict-violation-milestone-tiered", -7.3),
            NameStrictViolationMilestoneAdjuster(
                mutableMapOf<String, Double>("central" to 1.33, "east" to 0.76),
            ),
            PercentStrictViolationMilestoneAdjuster(-10.4),
            TieredStrictViolationMilestoneAdjuster(
                mutableListOf<Double>(5686.0, 9736.0),
                mutableListOf<Double>(0.85, 0.87),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 73066.6,
    )
}
