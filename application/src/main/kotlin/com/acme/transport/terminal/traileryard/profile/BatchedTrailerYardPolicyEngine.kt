package com.acme.transport.terminal.traileryard.profile

import com.acme.transport.kit.flags.Registry

/** One step of the batched trailer yard policy calculation. */
interface BatchedTrailerYardPolicyFactor {
    fun describe(): String
    fun adjust(item: BatchedTrailerYardPolicy, value: Double): Double
}

data class BatchedTrailerYardPolicyBatchSizeOffsetAdjuster(
    var weight: Double,
) : BatchedTrailerYardPolicyFactor {
    override fun describe(): String = "plus batch size times $weight"

    override fun adjust(item: BatchedTrailerYardPolicy, value: Double): Double {
        return value + item.batchSize.toDouble() * weight
    }
}

data class PercentBatchedTrailerYardPolicyAdjuster(var percent: Double) : BatchedTrailerYardPolicyFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedTrailerYardPolicy, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LabelBatchedTrailerYardPolicyAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedTrailerYardPolicyFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: BatchedTrailerYardPolicy, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedBatchedTrailerYardPolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedTrailerYardPolicyFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedTrailerYardPolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the batched trailer yard policy steps in order and keeps a trace of each. */
data class BatchedTrailerYardPolicyEngine(
    var steps: MutableList<BatchedTrailerYardPolicyFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: BatchedTrailerYardPolicy): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedTrailerYardPolicyFactor = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedTrailerYardPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultBatchedTrailerYardPolicyEngine(flags: Registry): BatchedTrailerYardPolicyEngine {
    return BatchedTrailerYardPolicyEngine(
        steps = mutableListOf<BatchedTrailerYardPolicyFactor>(
            BatchedTrailerYardPolicyBatchSizeOffsetAdjuster(0.69),
            PercentBatchedTrailerYardPolicyAdjuster(-3.4),
            LabelBatchedTrailerYardPolicyAdjuster(
                mutableMapOf<String, Double>("retail" to 1.39, "west" to 1.11),
            ),
            FlaggedBatchedTrailerYardPolicyAdjuster(flags, "batched-trailer-yard-policy-global", -2.3),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 75701.2,
    )
}
