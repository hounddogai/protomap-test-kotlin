package com.acme.transport.network.node.nodeagreement

import com.acme.transport.kit.flags.Registry

/** One step of the node profile calculation. */
interface NodeProfileAdjuster {
    fun describe(): String
    fun adjust(item: NodeProfile, value: Double): Double
}

data class MinimumNodeProfileAdjuster(var minimum: Double) : NodeProfileAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: NodeProfile, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class NodeProfileMaximumOffsetAdjuster(var weight: Double) : NodeProfileAdjuster {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: NodeProfile, value: Double): Double = value + item.maximum * weight
}

data class CapNodeProfileAdjuster(var cap: Double) : NodeProfileAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: NodeProfile, value: Double): Double = minOf(value, cap)
}

data class FlaggedNodeProfileAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : NodeProfileAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: NodeProfile, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the node profile steps in order and keeps a trace of each. */
data class NodeProfileEngine(
    var steps: MutableList<NodeProfileAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: NodeProfile): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: NodeProfileAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<NodeProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultNodeProfileEngine(flags: Registry): NodeProfileEngine {
    return NodeProfileEngine(
        steps = mutableListOf<NodeProfileAdjuster>(
            MinimumNodeProfileAdjuster(39.1),
            NodeProfileMaximumOffsetAdjuster(1.14),
            CapNodeProfileAdjuster(41955.0),
            FlaggedNodeProfileAdjuster(flags, "node-profile-capped", 1.1),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 15301.8,
    )
}
