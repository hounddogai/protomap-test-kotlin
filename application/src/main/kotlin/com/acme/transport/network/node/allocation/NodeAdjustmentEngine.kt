package com.acme.transport.network.node.allocation

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the node adjustment calculation. */
interface NodeAdjustmentAdjuster {
    fun describe(): String
    fun adjust(item: NodeAdjustment, value: Double): Double
}

data class MinimumNodeAdjustmentAdjuster(var minimum: Double) : NodeAdjustmentAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: NodeAdjustment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentNodeAdjustmentAdjuster(var percent: Double) : NodeAdjustmentAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: NodeAdjustment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class NodeAdjustmentEtaMinutesOffsetAdjuster(var weight: Double) : NodeAdjustmentAdjuster {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: NodeAdjustment, value: Double): Double = value + item.etaMinutes.toDouble() * weight
}

data class TieredNodeAdjustmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : NodeAdjustmentAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: NodeAdjustment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the node adjustment steps in order and keeps a trace of each. */
data class NodeAdjustmentEngine(
    var steps: MutableList<NodeAdjustmentAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: NodeAdjustment): Double {
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

    fun evaluateAll(items: MutableList<NodeAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultNodeAdjustmentEngine(): NodeAdjustmentEngine {
    return NodeAdjustmentEngine(
        steps = mutableListOf<NodeAdjustmentAdjuster>(
            MinimumNodeAdjustmentAdjuster(7.2),
            PercentNodeAdjustmentAdjuster(-5.8),
            NodeAdjustmentEtaMinutesOffsetAdjuster(1.25),
            TieredNodeAdjustmentAdjuster(
                mutableListOf<Double>(2796.0, 2978.0),
                mutableListOf<Double>(1.1, 0.84),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 19461.3,
    )
}
