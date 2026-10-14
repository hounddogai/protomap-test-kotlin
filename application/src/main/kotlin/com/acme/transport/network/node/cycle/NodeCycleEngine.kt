package com.acme.transport.network.node.cycle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the node cycle calculation. */
interface NodeCycleStep {
    fun describe(): String
    fun adjust(item: NodeCycle, value: Double): Double
}

data class FlaggedNodeCycleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : NodeCycleStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: NodeCycle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumNodeCycleAdjuster(var minimum: Double) : NodeCycleStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: NodeCycle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredNodeCycleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : NodeCycleStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: NodeCycle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentNodeCycleAdjuster(var percent: Double) : NodeCycleStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: NodeCycle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class NodeCycleGraceDaysOffsetAdjuster(var weight: Double) : NodeCycleStep {
    override fun describe(): String = "plus grace days times $weight"

    override fun adjust(item: NodeCycle, value: Double): Double = value + item.graceDays.toDouble() * weight
}

/** Runs the node cycle steps in order and keeps a trace of each. */
data class NodeCycleEngine(
    var steps: MutableList<NodeCycleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: NodeCycle): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<NodeCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultNodeCycleEngine(flags: Registry): NodeCycleEngine {
    return NodeCycleEngine(
        steps = mutableListOf<NodeCycleStep>(
            FlaggedNodeCycleAdjuster(flags, "node-cycle-weighted", -0.7),
            MinimumNodeCycleAdjuster(19.2),
            TieredNodeCycleAdjuster(
                mutableListOf<Double>(1674.0, 2935.0, 3673.0, 3744.0),
                mutableListOf<Double>(1.01, 1.09, 0.96, 1.02),
            ),
            PercentNodeCycleAdjuster(6.3),
            NodeCycleGraceDaysOffsetAdjuster(1.94),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 6189.9,
    )
}
