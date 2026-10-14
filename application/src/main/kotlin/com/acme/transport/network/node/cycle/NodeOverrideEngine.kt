package com.acme.transport.network.node.cycle

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the node override calculation. */
interface NodeOverrideAdjuster {
    fun describe(): String
    fun adjust(item: NodeOverride, value: Double): Double
}

data class TitleNodeOverrideAdjuster(var factors: MutableMap<String, Double>) : NodeOverrideAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: NodeOverride, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredNodeOverrideAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : NodeOverrideAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: NodeOverride, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingNodeOverrideAdjuster(var step: Double) : NodeOverrideAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: NodeOverride, value: Double): Double = roundTo(value, step)
}

data class NodeOverrideScoreOffsetAdjuster(var weight: Double) : NodeOverrideAdjuster {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: NodeOverride, value: Double): Double = value + item.score * weight
}

/** Runs the node override steps in order and keeps a trace of each. */
data class NodeOverrideEngine(
    var steps: MutableList<NodeOverrideAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: NodeOverride): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<NodeOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultNodeOverrideEngine(): NodeOverrideEngine {
    return NodeOverrideEngine(
        steps = mutableListOf<NodeOverrideAdjuster>(
            TitleNodeOverrideAdjuster(mutableMapOf<String, Double>("retail" to 1.1, "west" to 0.84)),
            TieredNodeOverrideAdjuster(
                mutableListOf<Double>(1613.0, 3283.0, 7597.0),
                mutableListOf<Double>(1.13, 0.88, 0.8),
            ),
            RoundingNodeOverrideAdjuster(0.1),
            NodeOverrideScoreOffsetAdjuster(1.87),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 51980.5,
    )
}
