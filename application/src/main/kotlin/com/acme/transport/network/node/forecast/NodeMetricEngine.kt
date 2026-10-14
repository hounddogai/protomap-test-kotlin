package com.acme.transport.network.node.forecast

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the node metric calculation. */
interface NodeMetricStep {
    fun describe(): String
    fun adjust(item: NodeMetric, value: Double): Double
}

data class MinimumNodeMetricAdjuster(var minimum: Double) : NodeMetricStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: NodeMetric, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapNodeMetricAdjuster(var cap: Double) : NodeMetricStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: NodeMetric, value: Double): Double = minOf(value, cap)
}

data class TieredNodeMetricAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : NodeMetricStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: NodeMetric, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedNodeMetricAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : NodeMetricStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: NodeMetric, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingNodeMetricAdjuster(var step: Double) : NodeMetricStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: NodeMetric, value: Double): Double = roundTo(value, step)
}

/** Runs the node metric steps in order and keeps a trace of each. */
data class NodeMetricEngine(
    var steps: MutableList<NodeMetricStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: NodeMetric): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<NodeMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultNodeMetricEngine(flags: Registry): NodeMetricEngine {
    return NodeMetricEngine(
        steps = mutableListOf<NodeMetricStep>(
            MinimumNodeMetricAdjuster(39.8),
            CapNodeMetricAdjuster(31582.0),
            TieredNodeMetricAdjuster(
                mutableListOf<Double>(10.0, 380.0, 5554.0, 8952.0),
                mutableListOf<Double>(0.96, 1.18, 0.94, 1.01),
            ),
            FlaggedNodeMetricAdjuster(flags, "node-metric-regional", 6.1),
            RoundingNodeMetricAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 38149.6,
    )
}
