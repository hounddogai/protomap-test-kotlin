package com.acme.transport.safety.coaching.mapping

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the regional coaching milestone calculation. */
interface RegionalCoachingMilestoneFactor {
    fun describe(): String
    fun adjust(item: RegionalCoachingMilestone, value: Double): Double
}

data class MinimumRegionalCoachingMilestoneAdjuster(var minimum: Double) : RegionalCoachingMilestoneFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalCoachingMilestone, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedRegionalCoachingMilestoneAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalCoachingMilestoneFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalCoachingMilestone, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RegionalCoachingMilestonePalletsOffsetAdjuster(
    var weight: Double,
) : RegionalCoachingMilestoneFactor {
    override fun describe(): String = "plus pallets times $weight"

    override fun adjust(item: RegionalCoachingMilestone, value: Double): Double {
        return value + item.pallets.toDouble() * weight
    }
}

data class PercentRegionalCoachingMilestoneAdjuster(var percent: Double) : RegionalCoachingMilestoneFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalCoachingMilestone, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredRegionalCoachingMilestoneAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalCoachingMilestoneFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalCoachingMilestone, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class ChannelRegionalCoachingMilestoneAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalCoachingMilestoneFactor {
    override fun describe(): String = "by channel"

    override fun adjust(item: RegionalCoachingMilestone, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the regional coaching milestone steps in order and keeps a trace of each. */
data class RegionalCoachingMilestoneEngine(
    var steps: MutableList<RegionalCoachingMilestoneFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RegionalCoachingMilestone): Double {
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

    fun evaluateAll(items: MutableList<RegionalCoachingMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRegionalCoachingMilestoneEngine(flags: Registry): RegionalCoachingMilestoneEngine {
    return RegionalCoachingMilestoneEngine(
        steps = mutableListOf<RegionalCoachingMilestoneFactor>(
            MinimumRegionalCoachingMilestoneAdjuster(21.8),
            FlaggedRegionalCoachingMilestoneAdjuster(flags, "regional-coaching-milestone-regional", 1.4),
            RegionalCoachingMilestonePalletsOffsetAdjuster(0.89),
            PercentRegionalCoachingMilestoneAdjuster(18.3),
            TieredRegionalCoachingMilestoneAdjuster(
                mutableListOf<Double>(2464.0, 7946.0),
                mutableListOf<Double>(1.12, 1.2),
            ),
            ChannelRegionalCoachingMilestoneAdjuster(
                mutableMapOf<String, Double>("retail" to 0.9, "central" to 0.92, "south" to 0.81),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 49190.0,
    )
}
