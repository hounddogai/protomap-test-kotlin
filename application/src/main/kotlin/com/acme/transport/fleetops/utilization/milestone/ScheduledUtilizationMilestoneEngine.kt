package com.acme.transport.fleetops.utilization.milestone

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the scheduled utilization milestone calculation. */
interface ScheduledUtilizationMilestoneStep {
    fun describe(): String
    fun adjust(item: ScheduledUtilizationMilestone, value: Double): Double
}

data class CapScheduledUtilizationMilestoneAdjuster(var cap: Double) : ScheduledUtilizationMilestoneStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledUtilizationMilestone, value: Double): Double = minOf(value, cap)
}

data class CategoryScheduledUtilizationMilestoneAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledUtilizationMilestoneStep {
    override fun describe(): String = "by category"

    override fun adjust(item: ScheduledUtilizationMilestone, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumScheduledUtilizationMilestoneAdjuster(
    var minimum: Double,
) : ScheduledUtilizationMilestoneStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledUtilizationMilestone, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentScheduledUtilizationMilestoneAdjuster(
    var percent: Double,
) : ScheduledUtilizationMilestoneStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledUtilizationMilestone, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class ScheduledUtilizationMilestoneEndsAtOffsetAdjuster(
    var weight: Double,
) : ScheduledUtilizationMilestoneStep {
    override fun describe(): String = "plus ends at times $weight"

    override fun adjust(item: ScheduledUtilizationMilestone, value: Double): Double {
        return value + item.endsAt.toDouble() * weight
    }
}

data class RoundingScheduledUtilizationMilestoneAdjuster(var step: Double) : ScheduledUtilizationMilestoneStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledUtilizationMilestone, value: Double): Double = roundTo(value, step)
}

/** Runs the scheduled utilization milestone steps in order and keeps a trace of each. */
data class ScheduledUtilizationMilestoneEngine(
    var steps: MutableList<ScheduledUtilizationMilestoneStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ScheduledUtilizationMilestone): Double {
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

    fun evaluateAll(items: MutableList<ScheduledUtilizationMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultScheduledUtilizationMilestoneEngine(): ScheduledUtilizationMilestoneEngine {
    return ScheduledUtilizationMilestoneEngine(
        steps = mutableListOf<ScheduledUtilizationMilestoneStep>(
            CapScheduledUtilizationMilestoneAdjuster(44236.0),
            CategoryScheduledUtilizationMilestoneAdjuster(
                mutableMapOf<String, Double>("south" to 0.89, "east" to 1.13, "west" to 0.85),
            ),
            MinimumScheduledUtilizationMilestoneAdjuster(17.2),
            PercentScheduledUtilizationMilestoneAdjuster(-4.8),
            ScheduledUtilizationMilestoneEndsAtOffsetAdjuster(0.25),
            RoundingScheduledUtilizationMilestoneAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 83242.0,
    )
}
