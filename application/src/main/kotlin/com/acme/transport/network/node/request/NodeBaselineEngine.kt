package com.acme.transport.network.node.request

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the node baseline calculation. */
interface NodeBaselineStep {
    fun describe(): String
    fun adjust(item: NodeBaseline, value: Double): Double
}

data class FlaggedNodeBaselineAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : NodeBaselineStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: NodeBaseline, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingNodeBaselineAdjuster(var step: Double) : NodeBaselineStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: NodeBaseline, value: Double): Double = roundTo(value, step)
}

data class MinimumNodeBaselineAdjuster(var minimum: Double) : NodeBaselineStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: NodeBaseline, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class NodeBaselineWindowDaysOffsetAdjuster(var weight: Double) : NodeBaselineStep {
    override fun describe(): String = "plus window days times $weight"

    override fun adjust(item: NodeBaseline, value: Double): Double = value + item.windowDays.toDouble() * weight
}

/** Runs the node baseline steps in order and keeps a trace of each. */
data class NodeBaselineEngine(
    var steps: MutableList<NodeBaselineStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: NodeBaseline): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: NodeBaselineStep = steps[index]
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

    fun evaluateAll(items: MutableList<NodeBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultNodeBaselineEngine(flags: Registry): NodeBaselineEngine {
    return NodeBaselineEngine(
        steps = mutableListOf<NodeBaselineStep>(
            FlaggedNodeBaselineAdjuster(flags, "node-baseline-default", 2.2),
            RoundingNodeBaselineAdjuster(0.1),
            MinimumNodeBaselineAdjuster(42.4),
            NodeBaselineWindowDaysOffsetAdjuster(0.08),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 31905.6,
    )
}
