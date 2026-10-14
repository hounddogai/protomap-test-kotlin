package com.acme.transport.fleetops.utilization.exception

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the utilization milestone calculation. */
interface UtilizationMilestoneFactor {
    fun describe(): String
    fun adjust(item: UtilizationMilestone, value: Double): Double
}

data class SegmentUtilizationMilestoneAdjuster(
    var factors: MutableMap<String, Double>,
) : UtilizationMilestoneFactor {
    override fun describe(): String = "by segment"

    override fun adjust(item: UtilizationMilestone, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredUtilizationMilestoneAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : UtilizationMilestoneFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: UtilizationMilestone, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedUtilizationMilestoneAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : UtilizationMilestoneFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: UtilizationMilestone, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapUtilizationMilestoneAdjuster(var cap: Double) : UtilizationMilestoneFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: UtilizationMilestone, value: Double): Double = minOf(value, cap)
}

data class MinimumUtilizationMilestoneAdjuster(var minimum: Double) : UtilizationMilestoneFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: UtilizationMilestone, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the utilization milestone steps in order and keeps a trace of each. */
data class UtilizationMilestoneEngine(
    var steps: MutableList<UtilizationMilestoneFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: UtilizationMilestone): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<UtilizationMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultUtilizationMilestoneEngine(flags: Registry): UtilizationMilestoneEngine {
    return UtilizationMilestoneEngine(
        steps = mutableListOf<UtilizationMilestoneFactor>(
            SegmentUtilizationMilestoneAdjuster(
                mutableMapOf<String, Double>("retail" to 0.98, "online" to 0.84, "west" to 1.34),
            ),
            TieredUtilizationMilestoneAdjuster(
                mutableListOf<Double>(7193.0, 8804.0),
                mutableListOf<Double>(1.14, 1.05),
            ),
            FlaggedUtilizationMilestoneAdjuster(flags, "utilization-milestone-global", 1.8),
            CapUtilizationMilestoneAdjuster(21086.0),
            MinimumUtilizationMilestoneAdjuster(35.2),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 51487.5,
    )
}
