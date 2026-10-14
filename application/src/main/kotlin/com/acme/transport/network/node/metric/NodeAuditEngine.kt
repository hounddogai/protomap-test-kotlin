package com.acme.transport.network.node.metric

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the node audit calculation. */
interface NodeAuditFactor {
    fun describe(): String
    fun adjust(item: NodeAudit, value: Double): Double
}

data class CapNodeAuditAdjuster(var cap: Double) : NodeAuditFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: NodeAudit, value: Double): Double = minOf(value, cap)
}

data class SegmentNodeAuditAdjuster(var factors: MutableMap<String, Double>) : NodeAuditFactor {
    override fun describe(): String = "by segment"

    override fun adjust(item: NodeAudit, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentNodeAuditAdjuster(var percent: Double) : NodeAuditFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: NodeAudit, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingNodeAuditAdjuster(var step: Double) : NodeAuditFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: NodeAudit, value: Double): Double = roundTo(value, step)
}

data class FlaggedNodeAuditAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : NodeAuditFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: NodeAudit, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class NodeAuditLoadKgOffsetAdjuster(var weight: Double) : NodeAuditFactor {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: NodeAudit, value: Double): Double = value + item.loadKg * weight
}

/** Runs the node audit steps in order and keeps a trace of each. */
data class NodeAuditEngine(
    var steps: MutableList<NodeAuditFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: NodeAudit): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: NodeAuditFactor = steps[index]
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

    fun evaluateAll(items: MutableList<NodeAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultNodeAuditEngine(flags: Registry): NodeAuditEngine {
    return NodeAuditEngine(
        steps = mutableListOf<NodeAuditFactor>(
            CapNodeAuditAdjuster(28506.0),
            SegmentNodeAuditAdjuster(mutableMapOf<String, Double>("west" to 0.98)),
            PercentNodeAuditAdjuster(20.3),
            RoundingNodeAuditAdjuster(0.25),
            FlaggedNodeAuditAdjuster(flags, "node-audit-default", -5.7),
            NodeAuditLoadKgOffsetAdjuster(1.75),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 16059.9,
    )
}
