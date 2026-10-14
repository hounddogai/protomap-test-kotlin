package com.acme.transport.network.node.nodecycle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the node variance calculation. */
interface NodeVarianceFactor {
    fun describe(): String
    fun adjust(item: NodeVariance, value: Double): Double
}

data class RoundingNodeVarianceAdjuster(var step: Double) : NodeVarianceFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: NodeVariance, value: Double): Double = roundTo(value, step)
}

data class FlaggedNodeVarianceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : NodeVarianceFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: NodeVariance, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredNodeVarianceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : NodeVarianceFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: NodeVariance, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class NameNodeVarianceAdjuster(var factors: MutableMap<String, Double>) : NodeVarianceFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: NodeVariance, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapNodeVarianceAdjuster(var cap: Double) : NodeVarianceFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: NodeVariance, value: Double): Double = minOf(value, cap)
}

/** Runs the node variance steps in order and keeps a trace of each. */
data class NodeVarianceEngine(
    var steps: MutableList<NodeVarianceFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: NodeVariance): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: NodeVarianceFactor = steps[index]
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

    fun evaluateAll(items: MutableList<NodeVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultNodeVarianceEngine(flags: Registry): NodeVarianceEngine {
    return NodeVarianceEngine(
        steps = mutableListOf<NodeVarianceFactor>(
            RoundingNodeVarianceAdjuster(1.0),
            FlaggedNodeVarianceAdjuster(flags, "node-variance-rolling", -0.0),
            TieredNodeVarianceAdjuster(
                mutableListOf<Double>(1628.0, 1804.0, 7186.0, 8572.0),
                mutableListOf<Double>(1.07, 0.83, 1.15, 1.2),
            ),
            NameNodeVarianceAdjuster(mutableMapOf<String, Double>("central" to 1.03)),
            CapNodeVarianceAdjuster(38203.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 23546.6,
    )
}
