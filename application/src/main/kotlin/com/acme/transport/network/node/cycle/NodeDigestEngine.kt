package com.acme.transport.network.node.cycle

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the node digest calculation. */
interface NodeDigestFactor {
    fun describe(): String
    fun adjust(item: NodeDigest, value: Double): Double
}

data class TieredNodeDigestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : NodeDigestFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: NodeDigest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CountryNodeDigestAdjuster(var factors: MutableMap<String, Double>) : NodeDigestFactor {
    override fun describe(): String = "by country"

    override fun adjust(item: NodeDigest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentNodeDigestAdjuster(var percent: Double) : NodeDigestFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: NodeDigest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumNodeDigestAdjuster(var minimum: Double) : NodeDigestFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: NodeDigest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingNodeDigestAdjuster(var step: Double) : NodeDigestFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: NodeDigest, value: Double): Double = roundTo(value, step)
}

data class CapNodeDigestAdjuster(var cap: Double) : NodeDigestFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: NodeDigest, value: Double): Double = minOf(value, cap)
}

/** Runs the node digest steps in order and keeps a trace of each. */
data class NodeDigestEngine(
    var steps: MutableList<NodeDigestFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: NodeDigest): Double {
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

    fun evaluateAll(items: MutableList<NodeDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultNodeDigestEngine(): NodeDigestEngine {
    return NodeDigestEngine(
        steps = mutableListOf<NodeDigestFactor>(
            TieredNodeDigestAdjuster(mutableListOf<Double>(6949.0, 9709.0), mutableListOf<Double>(1.18, 1.06)),
            CountryNodeDigestAdjuster(
                mutableMapOf<String, Double>("south" to 0.97, "central" to 1.0, "online" to 1.29),
            ),
            PercentNodeDigestAdjuster(-6.6),
            MinimumNodeDigestAdjuster(3.6),
            RoundingNodeDigestAdjuster(0.5),
            CapNodeDigestAdjuster(23568.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 9132.3,
    )
}
