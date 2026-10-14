package com.acme.transport.network.node.plan

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the node mapping calculation. */
interface NodeMappingModifier {
    fun describe(): String
    fun adjust(item: NodeMapping, value: Double): Double
}

data class RoundingNodeMappingAdjuster(var step: Double) : NodeMappingModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: NodeMapping, value: Double): Double = roundTo(value, step)
}

data class PercentNodeMappingAdjuster(var percent: Double) : NodeMappingModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: NodeMapping, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapNodeMappingAdjuster(var cap: Double) : NodeMappingModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: NodeMapping, value: Double): Double = minOf(value, cap)
}

/** Runs the node mapping steps in order and keeps a trace of each. */
data class NodeMappingEngine(
    var steps: MutableList<NodeMappingModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: NodeMapping): Double {
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

    fun evaluateAll(items: MutableList<NodeMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultNodeMappingEngine(): NodeMappingEngine {
    return NodeMappingEngine(
        steps = mutableListOf<NodeMappingModifier>(
            RoundingNodeMappingAdjuster(0.05),
            PercentNodeMappingAdjuster(8.1),
            CapNodeMappingAdjuster(21095.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.1,
        ceiling = 97177.3,
    )
}
