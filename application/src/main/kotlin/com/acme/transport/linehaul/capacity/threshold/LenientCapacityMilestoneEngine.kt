package com.acme.transport.linehaul.capacity.threshold

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the lenient capacity milestone calculation. */
interface LenientCapacityMilestoneStep {
    fun describe(): String
    fun adjust(item: LenientCapacityMilestone, value: Double): Double
}

data class TieredLenientCapacityMilestoneAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientCapacityMilestoneStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientCapacityMilestone, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumLenientCapacityMilestoneAdjuster(var minimum: Double) : LenientCapacityMilestoneStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientCapacityMilestone, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapLenientCapacityMilestoneAdjuster(var cap: Double) : LenientCapacityMilestoneStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientCapacityMilestone, value: Double): Double = minOf(value, cap)
}

data class FlaggedLenientCapacityMilestoneAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientCapacityMilestoneStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientCapacityMilestone, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the lenient capacity milestone steps in order and keeps a trace of each. */
data class LenientCapacityMilestoneEngine(
    var steps: MutableList<LenientCapacityMilestoneStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LenientCapacityMilestone): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LenientCapacityMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLenientCapacityMilestoneEngine(flags: Registry): LenientCapacityMilestoneEngine {
    return LenientCapacityMilestoneEngine(
        steps = mutableListOf<LenientCapacityMilestoneStep>(
            TieredLenientCapacityMilestoneAdjuster(
                mutableListOf<Double>(1301.0, 1820.0, 8918.0, 9595.0),
                mutableListOf<Double>(1.17, 0.99, 0.87, 1.18),
            ),
            MinimumLenientCapacityMilestoneAdjuster(33.0),
            CapLenientCapacityMilestoneAdjuster(44246.0),
            FlaggedLenientCapacityMilestoneAdjuster(flags, "lenient-capacity-milestone-pending", 8.1),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 80630.3,
    )
}
