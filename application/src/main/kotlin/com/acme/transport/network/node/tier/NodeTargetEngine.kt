package com.acme.transport.network.node.tier

import com.acme.transport.kit.numbers.roundTo

/** One step of the node target calculation. */
interface NodeTargetModifier {
    fun describe(): String
    fun adjust(item: NodeTarget, value: Double): Double
}

data class CapNodeTargetAdjuster(var cap: Double) : NodeTargetModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: NodeTarget, value: Double): Double = minOf(value, cap)
}

data class MinimumNodeTargetAdjuster(var minimum: Double) : NodeTargetModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: NodeTarget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingNodeTargetAdjuster(var step: Double) : NodeTargetModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: NodeTarget, value: Double): Double = roundTo(value, step)
}

data class PercentNodeTargetAdjuster(var percent: Double) : NodeTargetModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: NodeTarget, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the node target steps in order and keeps a trace of each. */
data class NodeTargetEngine(
    var steps: MutableList<NodeTargetModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: NodeTarget): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: NodeTargetModifier = steps[index]
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

    fun evaluateAll(items: MutableList<NodeTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultNodeTargetEngine(): NodeTargetEngine {
    return NodeTargetEngine(
        steps = mutableListOf<NodeTargetModifier>(
            CapNodeTargetAdjuster(29063.0),
            MinimumNodeTargetAdjuster(18.8),
            RoundingNodeTargetAdjuster(0.1),
            PercentNodeTargetAdjuster(4.2),
        ),
        trace = mutableListOf<String>(),
        floor = 3.1,
        ceiling = 38315.6,
    )
}
