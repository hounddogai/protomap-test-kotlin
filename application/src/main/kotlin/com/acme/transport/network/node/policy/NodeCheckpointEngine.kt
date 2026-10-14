package com.acme.transport.network.node.policy

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the node checkpoint calculation. */
interface NodeCheckpointModifier {
    fun describe(): String
    fun adjust(item: NodeCheckpoint, value: Double): Double
}

data class RoundingNodeCheckpointAdjuster(var step: Double) : NodeCheckpointModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: NodeCheckpoint, value: Double): Double = roundTo(value, step)
}

data class TieredNodeCheckpointAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : NodeCheckpointModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: NodeCheckpoint, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class NameNodeCheckpointAdjuster(var factors: MutableMap<String, Double>) : NodeCheckpointModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: NodeCheckpoint, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class NodeCheckpointScoreOffsetAdjuster(var weight: Double) : NodeCheckpointModifier {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: NodeCheckpoint, value: Double): Double = value + item.score * weight
}

data class PercentNodeCheckpointAdjuster(var percent: Double) : NodeCheckpointModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: NodeCheckpoint, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the node checkpoint steps in order and keeps a trace of each. */
data class NodeCheckpointEngine(
    var steps: MutableList<NodeCheckpointModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: NodeCheckpoint): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<NodeCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultNodeCheckpointEngine(): NodeCheckpointEngine {
    return NodeCheckpointEngine(
        steps = mutableListOf<NodeCheckpointModifier>(
            RoundingNodeCheckpointAdjuster(0.25),
            TieredNodeCheckpointAdjuster(
                mutableListOf<Double>(6713.0, 9377.0),
                mutableListOf<Double>(0.95, 0.88),
            ),
            NameNodeCheckpointAdjuster(mutableMapOf<String, Double>("west" to 0.81)),
            NodeCheckpointScoreOffsetAdjuster(0.61),
            PercentNodeCheckpointAdjuster(-21.3),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 97940.1,
    )
}
