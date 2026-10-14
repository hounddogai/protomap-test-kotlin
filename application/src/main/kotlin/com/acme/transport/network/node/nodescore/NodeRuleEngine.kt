package com.acme.transport.network.node.nodescore

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the node rule calculation. */
interface NodeRuleAdjuster {
    fun describe(): String
    fun adjust(item: NodeRule, value: Double): Double
}

data class TieredNodeRuleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : NodeRuleAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: NodeRule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedNodeRuleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : NodeRuleAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: NodeRule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class NodeRuleLoadKgOffsetAdjuster(var weight: Double) : NodeRuleAdjuster {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: NodeRule, value: Double): Double = value + item.loadKg * weight
}

data class RoundingNodeRuleAdjuster(var step: Double) : NodeRuleAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: NodeRule, value: Double): Double = roundTo(value, step)
}

data class CapNodeRuleAdjuster(var cap: Double) : NodeRuleAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: NodeRule, value: Double): Double = minOf(value, cap)
}

data class MinimumNodeRuleAdjuster(var minimum: Double) : NodeRuleAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: NodeRule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the node rule steps in order and keeps a trace of each. */
data class NodeRuleEngine(
    var steps: MutableList<NodeRuleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: NodeRule): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<NodeRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultNodeRuleEngine(flags: Registry): NodeRuleEngine {
    return NodeRuleEngine(
        steps = mutableListOf<NodeRuleAdjuster>(
            TieredNodeRuleAdjuster(mutableListOf<Double>(8640.0, 9037.0), mutableListOf<Double>(1.12, 1.2)),
            FlaggedNodeRuleAdjuster(flags, "node-rule-capped", -8.4),
            NodeRuleLoadKgOffsetAdjuster(1.54),
            RoundingNodeRuleAdjuster(5.0),
            CapNodeRuleAdjuster(16422.0),
            MinimumNodeRuleAdjuster(47.9),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 58169.7,
    )
}
