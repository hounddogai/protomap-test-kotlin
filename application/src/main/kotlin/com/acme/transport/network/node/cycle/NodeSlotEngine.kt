package com.acme.transport.network.node.cycle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the node slot calculation. */
interface NodeSlotModifier {
    fun describe(): String
    fun adjust(item: NodeSlot, value: Double): Double
}

data class CapNodeSlotAdjuster(var cap: Double) : NodeSlotModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: NodeSlot, value: Double): Double = minOf(value, cap)
}

data class RoundingNodeSlotAdjuster(var step: Double) : NodeSlotModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: NodeSlot, value: Double): Double = roundTo(value, step)
}

data class LabelNodeSlotAdjuster(var factors: MutableMap<String, Double>) : NodeSlotModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: NodeSlot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedNodeSlotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : NodeSlotModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: NodeSlot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the node slot steps in order and keeps a trace of each. */
data class NodeSlotEngine(
    var steps: MutableList<NodeSlotModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: NodeSlot): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<NodeSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultNodeSlotEngine(flags: Registry): NodeSlotEngine {
    return NodeSlotEngine(
        steps = mutableListOf<NodeSlotModifier>(
            CapNodeSlotAdjuster(23851.0),
            RoundingNodeSlotAdjuster(0.05),
            LabelNodeSlotAdjuster(
                mutableMapOf<String, Double>("west" to 1.09, "online" to 1.19, "north" to 0.92),
            ),
            FlaggedNodeSlotAdjuster(flags, "node-slot-rolling", -2.3),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 81829.5,
    )
}
