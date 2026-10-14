package com.acme.transport.network.node.request

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the batched node request calculation. */
interface BatchedNodeRequestStep {
    fun describe(): String
    fun adjust(item: BatchedNodeRequest, value: Double): Double
}

data class MinimumBatchedNodeRequestAdjuster(var minimum: Double) : BatchedNodeRequestStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedNodeRequest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedBatchedNodeRequestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedNodeRequestStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedNodeRequest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class BatchedNodeRequestStartsAtOffsetAdjuster(var weight: Double) : BatchedNodeRequestStep {
    override fun describe(): String = "plus starts at times $weight"

    override fun adjust(item: BatchedNodeRequest, value: Double): Double = value + item.startsAt.toDouble() * weight
}

data class RoundingBatchedNodeRequestAdjuster(var step: Double) : BatchedNodeRequestStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedNodeRequest, value: Double): Double = roundTo(value, step)
}

/** Runs the batched node request steps in order and keeps a trace of each. */
data class BatchedNodeRequestEngine(
    var steps: MutableList<BatchedNodeRequestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BatchedNodeRequest): Double {
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

    fun evaluateAll(items: MutableList<BatchedNodeRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBatchedNodeRequestEngine(flags: Registry): BatchedNodeRequestEngine {
    return BatchedNodeRequestEngine(
        steps = mutableListOf<BatchedNodeRequestStep>(
            MinimumBatchedNodeRequestAdjuster(7.6),
            FlaggedBatchedNodeRequestAdjuster(flags, "batched-node-request-batched", -5.9),
            BatchedNodeRequestStartsAtOffsetAdjuster(1.06),
            RoundingBatchedNodeRequestAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 38785.3,
    )
}
