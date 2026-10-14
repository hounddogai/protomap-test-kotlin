package com.acme.transport.network.node.overrideitem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the batched node policy calculation. */
interface BatchedNodePolicyAdjuster {
    fun describe(): String
    fun adjust(item: BatchedNodePolicy, value: Double): Double
}

data class FlaggedBatchedNodePolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedNodePolicyAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedNodePolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentBatchedNodePolicyAdjuster(var percent: Double) : BatchedNodePolicyAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedNodePolicy, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredBatchedNodePolicyAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedNodePolicyAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedNodePolicy, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class BatchedNodePolicyScoreOffsetAdjuster(var weight: Double) : BatchedNodePolicyAdjuster {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: BatchedNodePolicy, value: Double): Double = value + item.score * weight
}

data class LabelBatchedNodePolicyAdjuster(var factors: MutableMap<String, Double>) : BatchedNodePolicyAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: BatchedNodePolicy, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumBatchedNodePolicyAdjuster(var minimum: Double) : BatchedNodePolicyAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedNodePolicy, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the batched node policy steps in order and keeps a trace of each. */
data class BatchedNodePolicyEngine(
    var steps: MutableList<BatchedNodePolicyAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: BatchedNodePolicy): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedNodePolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultBatchedNodePolicyEngine(flags: Registry): BatchedNodePolicyEngine {
    return BatchedNodePolicyEngine(
        steps = mutableListOf<BatchedNodePolicyAdjuster>(
            FlaggedBatchedNodePolicyAdjuster(flags, "batched-node-policy-strict", 7.6),
            PercentBatchedNodePolicyAdjuster(-5.9),
            TieredBatchedNodePolicyAdjuster(
                mutableListOf<Double>(3710.0, 5061.0),
                mutableListOf<Double>(1.16, 0.89),
            ),
            BatchedNodePolicyScoreOffsetAdjuster(0.9),
            LabelBatchedNodePolicyAdjuster(mutableMapOf<String, Double>("central" to 0.98)),
            MinimumBatchedNodePolicyAdjuster(18.1),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 49121.5,
    )
}
