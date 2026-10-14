package com.acme.transport.network.node.cycle

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the node request calculation. */
interface NodeRequestModifier {
    fun describe(): String
    fun adjust(item: NodeRequest, value: Double): Double
}

data class CapNodeRequestAdjuster(var cap: Double) : NodeRequestModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: NodeRequest, value: Double): Double = minOf(value, cap)
}

data class TieredNodeRequestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : NodeRequestModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: NodeRequest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class NodeRequestBudgetOffsetAdjuster(var weight: Double) : NodeRequestModifier {
    override fun describe(): String = "plus budget times $weight"

    override fun adjust(item: NodeRequest, value: Double): Double = value + item.budget * weight
}

data class MinimumNodeRequestAdjuster(var minimum: Double) : NodeRequestModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: NodeRequest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the node request steps in order and keeps a trace of each. */
data class NodeRequestEngine(
    var steps: MutableList<NodeRequestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: NodeRequest): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<NodeRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultNodeRequestEngine(): NodeRequestEngine {
    return NodeRequestEngine(
        steps = mutableListOf<NodeRequestModifier>(
            CapNodeRequestAdjuster(40576.0),
            TieredNodeRequestAdjuster(mutableListOf<Double>(1253.0, 5993.0), mutableListOf<Double>(0.96, 0.82)),
            NodeRequestBudgetOffsetAdjuster(1.53),
            MinimumNodeRequestAdjuster(11.5),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 65792.2,
    )
}
