package com.acme.transport.network.node.slot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the batched node notice calculation. */
interface BatchedNodeNoticeModifier {
    fun describe(): String
    fun adjust(item: BatchedNodeNotice, value: Double): Double
}

data class RoundingBatchedNodeNoticeAdjuster(var step: Double) : BatchedNodeNoticeModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedNodeNotice, value: Double): Double = roundTo(value, step)
}

data class TieredBatchedNodeNoticeAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedNodeNoticeModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedNodeNotice, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class BatchedNodeNoticeStopCountOffsetAdjuster(var weight: Double) : BatchedNodeNoticeModifier {
    override fun describe(): String = "plus stop count times $weight"

    override fun adjust(item: BatchedNodeNotice, value: Double): Double = value + item.stopCount.toDouble() * weight
}

data class MinimumBatchedNodeNoticeAdjuster(var minimum: Double) : BatchedNodeNoticeModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedNodeNotice, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class ChannelBatchedNodeNoticeAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedNodeNoticeModifier {
    override fun describe(): String = "by channel"

    override fun adjust(item: BatchedNodeNotice, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedBatchedNodeNoticeAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedNodeNoticeModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedNodeNotice, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the batched node notice steps in order and keeps a trace of each. */
data class BatchedNodeNoticeEngine(
    var steps: MutableList<BatchedNodeNoticeModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: BatchedNodeNotice): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedNodeNoticeModifier = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedNodeNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultBatchedNodeNoticeEngine(flags: Registry): BatchedNodeNoticeEngine {
    return BatchedNodeNoticeEngine(
        steps = mutableListOf<BatchedNodeNoticeModifier>(
            RoundingBatchedNodeNoticeAdjuster(0.5),
            TieredBatchedNodeNoticeAdjuster(
                mutableListOf<Double>(169.0, 2398.0, 8110.0, 8692.0),
                mutableListOf<Double>(0.93, 1.19, 1.06, 0.99),
            ),
            BatchedNodeNoticeStopCountOffsetAdjuster(0.26),
            MinimumBatchedNodeNoticeAdjuster(18.7),
            ChannelBatchedNodeNoticeAdjuster(mutableMapOf<String, Double>("online" to 1.38, "retail" to 0.78)),
            FlaggedBatchedNodeNoticeAdjuster(flags, "batched-node-notice-adaptive", -0.7),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 13368.6,
    )
}
