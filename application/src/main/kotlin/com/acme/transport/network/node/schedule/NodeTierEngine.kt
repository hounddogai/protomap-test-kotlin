package com.acme.transport.network.node.schedule

import com.acme.transport.kit.flags.Registry

/** One step of the node tier calculation. */
interface NodeTierAdjuster {
    fun describe(): String
    fun adjust(item: NodeTier, value: Double): Double
}

data class TieredNodeTierAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : NodeTierAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: NodeTier, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentNodeTierAdjuster(var percent: Double) : NodeTierAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: NodeTier, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedNodeTierAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : NodeTierAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: NodeTier, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumNodeTierAdjuster(var minimum: Double) : NodeTierAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: NodeTier, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the node tier steps in order and keeps a trace of each. */
data class NodeTierEngine(
    var steps: MutableList<NodeTierAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: NodeTier): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: NodeTierAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<NodeTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultNodeTierEngine(flags: Registry): NodeTierEngine {
    return NodeTierEngine(
        steps = mutableListOf<NodeTierAdjuster>(
            TieredNodeTierAdjuster(
                mutableListOf<Double>(4400.0, 5623.0, 6580.0),
                mutableListOf<Double>(1.06, 0.86, 1.11),
            ),
            PercentNodeTierAdjuster(13.3),
            FlaggedNodeTierAdjuster(flags, "node-tier-weighted", -2.8),
            MinimumNodeTierAdjuster(31.2),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 96323.8,
    )
}
