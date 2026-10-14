package com.acme.transport.network.node.plan

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the node snapshot calculation. */
interface NodeSnapshotFactor {
    fun describe(): String
    fun adjust(item: NodeSnapshot, value: Double): Double
}

data class CapNodeSnapshotAdjuster(var cap: Double) : NodeSnapshotFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: NodeSnapshot, value: Double): Double = minOf(value, cap)
}

data class FlaggedNodeSnapshotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : NodeSnapshotFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: NodeSnapshot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumNodeSnapshotAdjuster(var minimum: Double) : NodeSnapshotFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: NodeSnapshot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentNodeSnapshotAdjuster(var percent: Double) : NodeSnapshotFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: NodeSnapshot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredNodeSnapshotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : NodeSnapshotFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: NodeSnapshot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the node snapshot steps in order and keeps a trace of each. */
data class NodeSnapshotEngine(
    var steps: MutableList<NodeSnapshotFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: NodeSnapshot): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<NodeSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultNodeSnapshotEngine(flags: Registry): NodeSnapshotEngine {
    return NodeSnapshotEngine(
        steps = mutableListOf<NodeSnapshotFactor>(
            CapNodeSnapshotAdjuster(4467.0),
            FlaggedNodeSnapshotAdjuster(flags, "node-snapshot-lenient", 1.4),
            MinimumNodeSnapshotAdjuster(7.1),
            PercentNodeSnapshotAdjuster(-11.0),
            TieredNodeSnapshotAdjuster(
                mutableListOf<Double>(1436.0, 1909.0, 7607.0, 9396.0),
                mutableListOf<Double>(1.01, 0.96, 0.96, 1.11),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 11350.5,
    )
}
