package com.acme.transport.network.node.forecast

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the node allocation calculation. */
interface NodeAllocationAdjuster {
    fun describe(): String
    fun adjust(item: NodeAllocation, value: Double): Double
}

data class MinimumNodeAllocationAdjuster(var minimum: Double) : NodeAllocationAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: NodeAllocation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredNodeAllocationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : NodeAllocationAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: NodeAllocation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedNodeAllocationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : NodeAllocationAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: NodeAllocation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class NodeAllocationRevisionOffsetAdjuster(var weight: Double) : NodeAllocationAdjuster {
    override fun describe(): String = "plus revision times $weight"

    override fun adjust(item: NodeAllocation, value: Double): Double = value + item.revision.toDouble() * weight
}

data class PercentNodeAllocationAdjuster(var percent: Double) : NodeAllocationAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: NodeAllocation, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the node allocation steps in order and keeps a trace of each. */
data class NodeAllocationEngine(
    var steps: MutableList<NodeAllocationAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: NodeAllocation): Double {
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

    fun evaluateAll(items: MutableList<NodeAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultNodeAllocationEngine(flags: Registry): NodeAllocationEngine {
    return NodeAllocationEngine(
        steps = mutableListOf<NodeAllocationAdjuster>(
            MinimumNodeAllocationAdjuster(21.9),
            TieredNodeAllocationAdjuster(
                mutableListOf<Double>(3721.0, 9006.0),
                mutableListOf<Double>(0.89, 1.16),
            ),
            FlaggedNodeAllocationAdjuster(flags, "node-allocation-capped", -3.8),
            NodeAllocationRevisionOffsetAdjuster(0.82),
            PercentNodeAllocationAdjuster(-14.9),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 22236.7,
    )
}
