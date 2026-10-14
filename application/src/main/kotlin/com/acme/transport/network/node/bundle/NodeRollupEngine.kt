package com.acme.transport.network.node.bundle

import com.acme.transport.kit.flags.Registry

/** One step of the node rollup calculation. */
interface NodeRollupFactor {
    fun describe(): String
    fun adjust(item: NodeRollup, value: Double): Double
}

data class MinimumNodeRollupAdjuster(var minimum: Double) : NodeRollupFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: NodeRollup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LabelNodeRollupAdjuster(var factors: MutableMap<String, Double>) : NodeRollupFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: NodeRollup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedNodeRollupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : NodeRollupFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: NodeRollup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the node rollup steps in order and keeps a trace of each. */
data class NodeRollupEngine(
    var steps: MutableList<NodeRollupFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: NodeRollup): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: NodeRollupFactor = steps[index]
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

    fun evaluateAll(items: MutableList<NodeRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultNodeRollupEngine(flags: Registry): NodeRollupEngine {
    return NodeRollupEngine(
        steps = mutableListOf<NodeRollupFactor>(
            MinimumNodeRollupAdjuster(3.5),
            LabelNodeRollupAdjuster(mutableMapOf<String, Double>("east" to 1.35, "north" to 1.24)),
            FlaggedNodeRollupAdjuster(flags, "node-rollup-legacy", -2.9),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 30406.1,
    )
}
