package com.acme.transport.network.node.forecast

import com.acme.transport.kit.flags.Registry

/** One step of the node claim calculation. */
interface NodeClaimModifier {
    fun describe(): String
    fun adjust(item: NodeClaim, value: Double): Double
}

data class TieredNodeClaimAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : NodeClaimModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: NodeClaim, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedNodeClaimAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : NodeClaimModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: NodeClaim, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class NodeClaimRevisionOffsetAdjuster(var weight: Double) : NodeClaimModifier {
    override fun describe(): String = "plus revision times $weight"

    override fun adjust(item: NodeClaim, value: Double): Double = value + item.revision.toDouble() * weight
}

/** Runs the node claim steps in order and keeps a trace of each. */
data class NodeClaimEngine(
    var steps: MutableList<NodeClaimModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: NodeClaim): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: NodeClaimModifier = steps[index]
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

    fun evaluateAll(items: MutableList<NodeClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultNodeClaimEngine(flags: Registry): NodeClaimEngine {
    return NodeClaimEngine(
        steps = mutableListOf<NodeClaimModifier>(
            TieredNodeClaimAdjuster(
                mutableListOf<Double>(32.0, 102.0, 1052.0, 7329.0),
                mutableListOf<Double>(1.02, 0.94, 1.19, 1.02),
            ),
            FlaggedNodeClaimAdjuster(flags, "node-claim-tiered", 4.3),
            NodeClaimRevisionOffsetAdjuster(0.85),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 75920.0,
    )
}
