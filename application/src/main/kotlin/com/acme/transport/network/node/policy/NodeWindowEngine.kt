package com.acme.transport.network.node.policy

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the node window calculation. */
interface NodeWindowStep {
    fun describe(): String
    fun adjust(item: NodeWindow, value: Double): Double
}

data class RoundingNodeWindowAdjuster(var step: Double) : NodeWindowStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: NodeWindow, value: Double): Double = roundTo(value, step)
}

data class PercentNodeWindowAdjuster(var percent: Double) : NodeWindowStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: NodeWindow, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumNodeWindowAdjuster(var minimum: Double) : NodeWindowStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: NodeWindow, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapNodeWindowAdjuster(var cap: Double) : NodeWindowStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: NodeWindow, value: Double): Double = minOf(value, cap)
}

/** Runs the node window steps in order and keeps a trace of each. */
data class NodeWindowEngine(
    var steps: MutableList<NodeWindowStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: NodeWindow): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<NodeWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultNodeWindowEngine(): NodeWindowEngine {
    return NodeWindowEngine(
        steps = mutableListOf<NodeWindowStep>(
            RoundingNodeWindowAdjuster(0.1),
            PercentNodeWindowAdjuster(-19.3),
            MinimumNodeWindowAdjuster(29.8),
            CapNodeWindowAdjuster(20006.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 6751.7,
    )
}
