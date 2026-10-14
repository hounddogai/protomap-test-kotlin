package com.acme.transport.network.node.seasonalnodeagreement

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the node notice calculation. */
interface NodeNoticeAdjuster {
    fun describe(): String
    fun adjust(item: NodeNotice, value: Double): Double
}

data class CapNodeNoticeAdjuster(var cap: Double) : NodeNoticeAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: NodeNotice, value: Double): Double = minOf(value, cap)
}

data class RoundingNodeNoticeAdjuster(var step: Double) : NodeNoticeAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: NodeNotice, value: Double): Double = roundTo(value, step)
}

data class TieredNodeNoticeAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : NodeNoticeAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: NodeNotice, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedNodeNoticeAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : NodeNoticeAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: NodeNotice, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentNodeNoticeAdjuster(var percent: Double) : NodeNoticeAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: NodeNotice, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumNodeNoticeAdjuster(var minimum: Double) : NodeNoticeAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: NodeNotice, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the node notice steps in order and keeps a trace of each. */
data class NodeNoticeEngine(
    var steps: MutableList<NodeNoticeAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: NodeNotice): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: NodeNoticeAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<NodeNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultNodeNoticeEngine(flags: Registry): NodeNoticeEngine {
    return NodeNoticeEngine(
        steps = mutableListOf<NodeNoticeAdjuster>(
            CapNodeNoticeAdjuster(29363.0),
            RoundingNodeNoticeAdjuster(1.0),
            TieredNodeNoticeAdjuster(
                mutableListOf<Double>(1806.0, 2784.0, 3644.0, 7504.0),
                mutableListOf<Double>(1.08, 1.02, 0.88, 0.89),
            ),
            FlaggedNodeNoticeAdjuster(flags, "node-notice-fallback", 8.9),
            PercentNodeNoticeAdjuster(-13.0),
            MinimumNodeNoticeAdjuster(16.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 17243.7,
    )
}
