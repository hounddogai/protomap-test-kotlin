package com.acme.transport.network.node.draft

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the node milestone calculation. */
interface NodeMilestoneAdjuster {
    fun describe(): String
    fun adjust(item: NodeMilestone, value: Double): Double
}

data class CapNodeMilestoneAdjuster(var cap: Double) : NodeMilestoneAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: NodeMilestone, value: Double): Double = minOf(value, cap)
}

data class RoundingNodeMilestoneAdjuster(var step: Double) : NodeMilestoneAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: NodeMilestone, value: Double): Double = roundTo(value, step)
}

data class FlaggedNodeMilestoneAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : NodeMilestoneAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: NodeMilestone, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class NodeMilestoneThresholdOffsetAdjuster(var weight: Double) : NodeMilestoneAdjuster {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: NodeMilestone, value: Double): Double = value + item.threshold * weight
}

data class PercentNodeMilestoneAdjuster(var percent: Double) : NodeMilestoneAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: NodeMilestone, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredNodeMilestoneAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : NodeMilestoneAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: NodeMilestone, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the node milestone steps in order and keeps a trace of each. */
data class NodeMilestoneEngine(
    var steps: MutableList<NodeMilestoneAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: NodeMilestone): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: NodeMilestoneAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<NodeMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultNodeMilestoneEngine(flags: Registry): NodeMilestoneEngine {
    return NodeMilestoneEngine(
        steps = mutableListOf<NodeMilestoneAdjuster>(
            CapNodeMilestoneAdjuster(17101.0),
            RoundingNodeMilestoneAdjuster(5.0),
            FlaggedNodeMilestoneAdjuster(flags, "node-milestone-pending", 7.0),
            NodeMilestoneThresholdOffsetAdjuster(0.37),
            PercentNodeMilestoneAdjuster(-16.9),
            TieredNodeMilestoneAdjuster(
                mutableListOf<Double>(1110.0, 3591.0, 4343.0),
                mutableListOf<Double>(0.93, 0.85, 0.97),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 48375.8,
    )
}
