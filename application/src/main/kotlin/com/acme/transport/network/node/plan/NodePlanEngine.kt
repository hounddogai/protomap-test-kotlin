package com.acme.transport.network.node.plan

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the node plan calculation. */
interface NodePlanStep {
    fun describe(): String
    fun adjust(item: NodePlan, value: Double): Double
}

data class NameNodePlanAdjuster(var factors: MutableMap<String, Double>) : NodePlanStep {
    override fun describe(): String = "by name"

    override fun adjust(item: NodePlan, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapNodePlanAdjuster(var cap: Double) : NodePlanStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: NodePlan, value: Double): Double = minOf(value, cap)
}

data class RoundingNodePlanAdjuster(var step: Double) : NodePlanStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: NodePlan, value: Double): Double = roundTo(value, step)
}

data class MinimumNodePlanAdjuster(var minimum: Double) : NodePlanStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: NodePlan, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the node plan steps in order and keeps a trace of each. */
data class NodePlanEngine(
    var steps: MutableList<NodePlanStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: NodePlan): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<NodePlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultNodePlanEngine(): NodePlanEngine {
    return NodePlanEngine(
        steps = mutableListOf<NodePlanStep>(
            NameNodePlanAdjuster(
                mutableMapOf<String, Double>("retail" to 1.23, "central" to 1.29, "online" to 1.09),
            ),
            CapNodePlanAdjuster(47419.0),
            RoundingNodePlanAdjuster(5.0),
            MinimumNodePlanAdjuster(45.3),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 51453.9,
    )
}
