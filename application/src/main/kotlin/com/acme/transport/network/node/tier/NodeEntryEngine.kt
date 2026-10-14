package com.acme.transport.network.node.tier

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the node entry calculation. */
interface NodeEntryModifier {
    fun describe(): String
    fun adjust(item: NodeEntry, value: Double): Double
}

data class RoundingNodeEntryAdjuster(var step: Double) : NodeEntryModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: NodeEntry, value: Double): Double = roundTo(value, step)
}

data class CapNodeEntryAdjuster(var cap: Double) : NodeEntryModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: NodeEntry, value: Double): Double = minOf(value, cap)
}

data class NameNodeEntryAdjuster(var factors: MutableMap<String, Double>) : NodeEntryModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: NodeEntry, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentNodeEntryAdjuster(var percent: Double) : NodeEntryModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: NodeEntry, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumNodeEntryAdjuster(var minimum: Double) : NodeEntryModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: NodeEntry, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the node entry steps in order and keeps a trace of each. */
data class NodeEntryEngine(
    var steps: MutableList<NodeEntryModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: NodeEntry): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<NodeEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultNodeEntryEngine(): NodeEntryEngine {
    return NodeEntryEngine(
        steps = mutableListOf<NodeEntryModifier>(
            RoundingNodeEntryAdjuster(5.0),
            CapNodeEntryAdjuster(6637.0),
            NameNodeEntryAdjuster(mutableMapOf<String, Double>("online" to 1.26, "west" to 1.07)),
            PercentNodeEntryAdjuster(1.8),
            MinimumNodeEntryAdjuster(31.3),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 92407.1,
    )
}
