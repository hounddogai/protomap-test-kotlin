package com.acme.transport.network.node.milestone

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the scheduled node milestone calculation. */
interface ScheduledNodeMilestoneModifier {
    fun describe(): String
    fun adjust(item: ScheduledNodeMilestone, value: Double): Double
}

data class MinimumScheduledNodeMilestoneAdjuster(var minimum: Double) : ScheduledNodeMilestoneModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledNodeMilestone, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedScheduledNodeMilestoneAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledNodeMilestoneModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledNodeMilestone, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class ScheduledNodeMilestoneAmountOffsetAdjuster(var weight: Double) : ScheduledNodeMilestoneModifier {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: ScheduledNodeMilestone, value: Double): Double = value + item.amount * weight
}

data class TieredScheduledNodeMilestoneAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledNodeMilestoneModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledNodeMilestone, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingScheduledNodeMilestoneAdjuster(var step: Double) : ScheduledNodeMilestoneModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledNodeMilestone, value: Double): Double = roundTo(value, step)
}

/** Runs the scheduled node milestone steps in order and keeps a trace of each. */
data class ScheduledNodeMilestoneEngine(
    var steps: MutableList<ScheduledNodeMilestoneModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ScheduledNodeMilestone): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ScheduledNodeMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultScheduledNodeMilestoneEngine(flags: Registry): ScheduledNodeMilestoneEngine {
    return ScheduledNodeMilestoneEngine(
        steps = mutableListOf<ScheduledNodeMilestoneModifier>(
            MinimumScheduledNodeMilestoneAdjuster(9.0),
            FlaggedScheduledNodeMilestoneAdjuster(flags, "scheduled-node-milestone-global", -6.6),
            ScheduledNodeMilestoneAmountOffsetAdjuster(1.32),
            TieredScheduledNodeMilestoneAdjuster(
                mutableListOf<Double>(4002.0, 6554.0, 8545.0, 9961.0),
                mutableListOf<Double>(0.97, 0.9, 0.83, 1.03),
            ),
            RoundingScheduledNodeMilestoneAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 72506.0,
    )
}
