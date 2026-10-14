package com.acme.transport.network.node.policy

import com.acme.transport.kit.numbers.roundTo

/** One step of the node policy calculation. */
interface NodePolicyStep {
    fun describe(): String
    fun adjust(item: NodePolicy, value: Double): Double
}

data class RoundingNodePolicyAdjuster(var step: Double) : NodePolicyStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: NodePolicy, value: Double): Double = roundTo(value, step)
}

data class CapNodePolicyAdjuster(var cap: Double) : NodePolicyStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: NodePolicy, value: Double): Double = minOf(value, cap)
}

data class MinimumNodePolicyAdjuster(var minimum: Double) : NodePolicyStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: NodePolicy, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the node policy steps in order and keeps a trace of each. */
data class NodePolicyEngine(
    var steps: MutableList<NodePolicyStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: NodePolicy): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: NodePolicyStep = steps[index]
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

    fun evaluateAll(items: MutableList<NodePolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultNodePolicyEngine(): NodePolicyEngine {
    return NodePolicyEngine(
        steps = mutableListOf<NodePolicyStep>(
            RoundingNodePolicyAdjuster(1.0),
            CapNodePolicyAdjuster(29619.0),
            MinimumNodePolicyAdjuster(10.8),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 23690.9,
    )
}
